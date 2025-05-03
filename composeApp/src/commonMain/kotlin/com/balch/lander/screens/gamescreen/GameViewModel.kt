package com.balch.lander.screens.gamescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balch.lander.GameConfig
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.PhysicsEngine
import com.balch.lander.core.game.TerrainGenerator
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.utils.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.lighthousegames.logging.logging
import kotlin.random.Random

/**
 * ViewModel for the Game Screen.
 * Handles game logic, physics, and state during gameplay.
 */
class GameViewModel(
    private val terrainGenerator: TerrainGenerator,
) : ViewModel() {
    private val logger = logging()

    // State flows for different components
    private val startGameIntentFlow = MutableSharedFlow<GameConfig>(
        replay = 1, extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val controlInputsFlow = MutableStateFlow(ControlInputs())

    /**
     * Sets the control inputs.
     */
    fun setControlsInputs(controlInputs: ControlInputs) {
        controlInputsFlow.tryEmit(controlInputs)
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
        logger.info { "Starting game with config: gravity=${config.gravity}, fuelLevel=${config.fuelLevel}, landingPadSize=${config.landingPadSize}" }
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
        ).also {
            logger.debug { "Initializing lander state : $it" }
        }
    }

    private fun generateTerrain(config: GameConfig): Terrain {
        logger.debug { "Generating terrain with width=${config.screenWidth}, height=${config.screenHeight}, landingPadSize=${config.landingPadSize}" }
        return terrainGenerator.generateTerrain(
            width = config.screenWidth,
            height = config.screenHeight,
            landingPadSize = config.landingPadSize,
            seed = Random.nextLong()
        ).also {
            logger.debug { "Terrain Generated  size: ${it.points.size} maxHeight: ${it.points.maxOf { it.y }}" }
        }
    }

    /**
     * Starts the game loop.
     */
    private fun startGameLoop(
        config: GameConfig,
        initialGameState: GameScreenState,
    ): Flow<GameScreenState> =
        flow {
            logger.info { "Starting game loop with gravity=${config.gravity}" }
            val physicsEngine = PhysicsEngine(config)

            // Initialize last update time
            var lastUpdateTime = TimeUtil.currentTimeMillis()
            var currentGameState = initialGameState
            var controlInputs = ControlInputs()

            while (currentGameState !is GameScreenState.GameOver) {
                // Calculate delta time
                val workStartTime = TimeUtil.currentTimeMillis()
                val deltaTimeMs = workStartTime - lastUpdateTime
                lastUpdateTime = workStartTime

                logger.verbose {
                    "Game Loop - Start controlInputs=$controlInputs deltaTimeMs=$deltaTimeMs"
                }

                // Update game state
                currentGameState = updatedGameState(
                    physicsEngine = physicsEngine,
                    deltaTimeMs = deltaTimeMs,
                    currentGameState = currentGameState,
                    controlInputs = controlInputs,
                )

                val workEndTime = TimeUtil.currentTimeMillis()
                val workTimeMs =
                    if (TimeUtil.isTimeAccurate) workEndTime - workStartTime
                    else 0
                val sleepTimeMs = maxOf(0L, 16L - workTimeMs)

                logger.verbose {
                    "Game Loop - End workTimeMs=${workTimeMs.takeIf { TimeUtil.isTimeAccurate } ?: "???" } sleepTimeMs=$sleepTimeMs State: $currentGameState"
                }
                emit(currentGameState)

                controlInputs = merge(
                    controlInputsFlow.drop(1) // wait for next control input
                        .onEach { logger.debug { "Game Loop - Control Inputs: $it" } },
                    flow {
                        // Delay to maintain frame rate (60 FPS)
                        delay(sleepTimeMs)
                        emit(controlInputs)
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

// Logger for game state updates
private val gameStateLogger = logging("GameState")

/**
 * Updates the game state based on physics and controls.
 * @param deltaTime Time elapsed since last update in seconds
 */
private fun updatedGameState(
    physicsEngine: PhysicsEngine,
    deltaTimeMs: Long,
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
        deltaTimeMs = deltaTimeMs,
        controls = controlInputs,
        terrain = currentGameState.environmentState.terrain,
        config = currentGameState.environmentState.config,
    )

    // Check if lander has landed or crashed
    return when (newLanderState.status) {
        GameStatus.PLAYING -> GameScreenState.Playing(
            landerState = newLanderState,
            environmentState = currentGameState.environmentState,
            fps = if (deltaTimeMs > 0) (1000L / deltaTimeMs).toInt() else 0,
        )

        GameStatus.LANDED -> {
            val message = successMessages.random()
            gameStateLogger.info { "Lander successfully landed! Message: $message" }
            GameScreenState.GameOver(true, message)
        }
        GameStatus.CRASHED -> {
            val message = failureMessages.random()
            gameStateLogger.info { "Lander crashed! Message: $message" }
            GameScreenState.GameOver(false, message)
        }
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
