package com.balch.lander.screens.gamescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balch.lander.GameConfig
import com.balch.lander.core.TimeUtil
import com.balch.lander.screens.gamescreen.gameplay.ControlInputs
import com.balch.lander.screens.gamescreen.gameplay.PhysicsEngine
import com.balch.lander.screens.gamescreen.gameplay.TerrainGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.random.Random

/**
 * ViewModel for the Game Screen.
 * Handles game logic, physics, and state during gameplay.
 */
class GameViewModel(
    private val terrainGenerator: TerrainGenerator,
) : ViewModel() {

    // State flows for different components
    private val startGameIntentFlow = MutableSharedFlow<GameConfig>(
        replay = 1, extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val controlInputsFlow = MutableStateFlow(ControlInputs())

    /**
     * Sets the thrust control input.
     * @param isThrusting Whether thrust is active
     */
    fun setThrust(isThrusting: Boolean) {
        controlInputsFlow.tryEmit(ControlInputs(thrust = isThrusting))
    }

    /**
     * Sets the rotate left control input.
     * @param isRotatingLeft Whether rotating left is active
     */
    fun setRotateLeft(isRotatingLeft: Boolean) {
        controlInputsFlow.tryEmit(ControlInputs(rotateLeft = isRotatingLeft))
    }

    /**
     * Sets the rotate right control input.
     * @param isRotatingRight Whether rotating right is active
     */
    fun setRotateRight(isRotatingRight: Boolean) {
        controlInputsFlow.tryEmit(ControlInputs(rotateRight = isRotatingRight))
    }

    // UI state derived from game state and UI state flow
    val uiState: StateFlow<GameScreenState> =
        startGameIntentFlow
            .transformLatest { config ->
                emit(GameScreenState.Loading)

                val terrain = generateTerrain(config)

                val environmentState = GameEnvironmentState(
                    terrain = terrain,
                    config = config
                )

                val landerState = initialLanderState(config)

                val initialGameState = GameScreenState.Playing(landerState, environmentState)
                emit(initialGameState)
                emitAll(startGameLoop(config, initialGameState))
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GameScreenState.Loading
            )

    fun startGame(config: GameConfig) {
        startGameIntentFlow.tryEmit(config)
    }

    private fun initialLanderState(config: GameConfig): LanderState {
        val initialFuel = 100f + config.fuelLevel * 100f // 100-200 units of fuel
        return LanderState(
            position = Vector2D(config.screenWidth / 2, 50f), // Start at top center
            velocity = Vector2D(0f, 0f),
            rotation = 0f,
            fuel = initialFuel,
            initialFuel = initialFuel
        )
    }

    private fun generateTerrain(config: GameConfig): Terrain =
        terrainGenerator.generateTerrain(
            width = config.screenWidth,
            height = config.screenHeight,
            landingPadSize = config.landingPadSize,
            seed = Random.nextLong()
        )

    /**
     * Starts the game loop.
     */
    private fun startGameLoop(
        config: GameConfig,
        initialGameState: GameScreenState,
    ): Flow<GameScreenState> =
        flow {
            val physicsEngine = PhysicsEngine(config)

            // Initialize last update time
            var lastUpdateTime = TimeUtil.currentTimeMillis()
            var currentGameState = initialGameState
            var controlInputs = ControlInputs()

            while (currentGameState !is GameScreenState.GameOver) {
                // Calculate delta time
                val currentTime = TimeUtil.currentTimeMillis()
                val deltaTime = (currentTime - lastUpdateTime) / 1000f
                lastUpdateTime = currentTime

                // Update game state
                currentGameState = updatedGameState(
                    physicsEngine = physicsEngine,
                    deltaTime = deltaTime,
                    currentGameState = currentGameState,
                    controlInputs = controlInputs,
                )

                emit(currentGameState)

                controlInputs = merge(
                    controlInputsFlow.drop(1),
                    flow {
                        // Delay to maintain frame rate (60 FPS)
                        delay(16)
                        controlInputs
                    }
                ).first()
            }
        }
}

// Gets a list of success messages
private val successMessages: List<String> =
    listOf(
        "Perfect landing! NASA would be proud.",
        "Touchdown! The Eagle has landed.",
        "Smooth as silk! You're a natural.",
        "Landing confirmed. Mission accomplished!",
        "That's one small step for a lander...",
        "Houston, we have a successful landing!"
    )

// Gets a list of failure messages
private val failureMessages: List<String> =
    listOf(
        "Houston, we have a problem.",
        "That's going to leave a mark...",
        "The lander is now a permanent lunar feature.",
        "Let's call that a 'rapid unscheduled disassembly'.",
        "Maybe try landing gear down next time?",
        "The moon claims another victim."
    )

/**
 * Updates the game state based on physics and controls.
 * @param deltaTime Time elapsed since last update in seconds
 */
private fun updatedGameState(
    physicsEngine: PhysicsEngine,
    deltaTime: Float,
    currentGameState: GameScreenState,
    controlInputs: ControlInputs,
): GameScreenState {

    // Skip update if game is not in playing state
    if (currentGameState !is GameScreenState.Playing) {
        return currentGameState
    }

    // Update game state using physics engine
    val newLanderState = physicsEngine.update(
        landerState = currentGameState.landerState,
        deltaTime = deltaTime,
        controls = controlInputs,
        terrain = currentGameState.environmentState.terrain
    )


    // Check if lander has landed or crashed
    return when (newLanderState.status) {
        GameStatus.PLAYING -> GameScreenState.Playing(
            landerState = newLanderState,
            environmentState = currentGameState.environmentState,
            fps = if (deltaTime > 0) (1f / deltaTime).toInt() else 0
        )

        GameStatus.LANDED -> GameScreenState.GameOver(true, successMessages.random())
        GameStatus.CRASHED -> GameScreenState.GameOver(false, failureMessages.random())
    }
}


/**
 * UI state for the Game Screen.
 */
sealed interface GameScreenState {

    data object Loading : GameScreenState
    data class Playing(
        /**
         * The dynamic state of the lander that changes with the game loop.
         */
        val landerState: LanderState = LanderState(),

        /**
         * The static state of the game environment that is generated at game start.
         */
        val environmentState: GameEnvironmentState = GameEnvironmentState(),

        /**
         * Current frames per second.
         */
        val fps: Int = 0
    ) : GameScreenState

    data object NavigateToStartScreen : GameScreenState

    data class GameOver(
        val isSuccess: Boolean,
        val message: String,
        /**
         * The dynamic state of the lander that changes with the game loop.
         */
        val landerState: LanderState = LanderState(),

        /**
         * The static state of the game environment that is generated at game start.
         */
        val environmentState: GameEnvironmentState = GameEnvironmentState(),
    ) : GameScreenState
}
