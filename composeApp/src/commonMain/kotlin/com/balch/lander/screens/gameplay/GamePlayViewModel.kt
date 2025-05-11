package com.balch.lander.screens.gameplay

import androidx.lifecycle.ViewModel
import com.balch.lander.GameConfig
import com.balch.lander.Platform
import com.balch.lander.core.coroutines.CoroutineScopeProvider
import com.balch.lander.core.coroutines.DispatcherProvider
import com.balch.lander.core.game.Camera
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.PhysicsEngine
import com.balch.lander.core.game.TerrainGenerator
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.game.sound.SoundService
import com.balch.lander.core.utils.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.lighthousegames.logging.KmLogging
import org.lighthousegames.logging.logging

/**
 * ViewModel for the Game Screen.
 * Handles game logic, physics, and state during gameplay.
 */
class GamePlayViewModel(
    private val platform: Platform,
    private val terrainGenerator: TerrainGenerator,
    private val timeProvider: TimeProvider,
    private val soundService: SoundService,
    dispatcherProvider: DispatcherProvider,
    scopeProvider: CoroutineScopeProvider,
) : ViewModel() {

    private val logger = logging()

    private val fpsHistory = mutableListOf<Int>()

    // State flows for different components
    private val startGameIntentFlow = MutableSharedFlow<GameConfig>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A shared flow used to emit and collect control inputs for the game.
     *
     * This SharedFlow is configured to support multiple ControlInput
     * emissions in rapid succession. This pattern is used instead of a
     * StateFlow which will conflate and drop multiple unhandled emissions.
     */
    private val controlInputsFlow = MutableSharedFlow<ControlInputs>(
        replay = 1,
        extraBufferCapacity = 1024,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val controlInputDistinctFlow = controlInputsFlow
        .distinctUntilChanged()

    /**
     * Sets the control inputs.
     */
    fun setControlsInputs(controlInputs: ControlInputs) {
        logger.v { "Setting control inputs: $controlInputs" }
        controlInputsFlow.tryEmit(controlInputs)
    }

    // UI state derived from game state and UI state flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GameScreenState> =
        startGameIntentFlow
            .transformLatest { config ->
                fpsHistory.clear()
                emit(GameScreenState.Loading)

                val initialGameState = GameScreenState.Playing(
                    landerState = initialLanderState(config),
                    environment = GameEnvironmentState(
                        terrain = generateTerrain(config),
                        config = config,
                        platform = platform
                    )
                )
                emit(initialGameState)
                emitAll(startGameLoop(config, initialGameState))
            }
            .flowOn(dispatcherProvider.default)
            .stateIn(
                scope = scopeProvider[this],
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GameScreenState.Loading
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val thrustStrengthFlow: StateFlow<ThrustStrength> =
        uiState
            .mapLatest { state ->
                if (state is GameScreenState.Playing) {
                    state.landerState.thrustStrength
                } else ThrustStrength.OFF
            }
            .flowOn(dispatcherProvider.default)
            .stateIn(
                scope = scopeProvider[this],
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ThrustStrength.OFF
            )

    init {
        // collect on this flow after its is created
        scopeProvider[this].launch {
            thrustStrengthFlow.collectLatest {
                soundService.playThrustSound(it)
            }
        }
    }

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
            initialFuel = initialFuel,
        ).also { state ->
            logger.debug { "Initializing lander state : $state" }
        }
    }

    private fun generateTerrain(config: GameConfig): Terrain {
        logger.debug { "Generating terrain with width=${config.screenWidth}, height=${config.screenHeight}, landingPadSize=${config.landingPadSize}" }
        return terrainGenerator.generateTerrain(
            width = config.screenWidth,
            height = config.screenHeight,
            landingPadSize = config.landingPadSize,
        ).also { state ->
            logger.debug {
                "Terrain Generated size: ${state.points.size} maxHeight: ${state.points.maxOf { it.y }}"
            }
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
            var lastUpdateTime = timeProvider.currentTimeMillis()
            var currentGameState = initialGameState
            var controlInputs = ControlInputs()

            while (currentGameState !is GameScreenState.GameOver) {
                // Calculate delta time
                val workStartTime = timeProvider.currentTimeMillis()
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
                    fps = averageFps(deltaTimeMs),
                )

                val workEndTime = timeProvider.currentTimeMillis()
                val workTimeMs =
                    if (timeProvider.isTimeAccurate) workEndTime - workStartTime
                    else 0
                val sleepTimeMs = maxOf(0L, 16L - workTimeMs)

                logger.verbose {
                    "Game Loop - End workTimeMs=${workTimeMs.takeIf { timeProvider.isTimeAccurate } ?: "???"} sleepTimeMs=$sleepTimeMs State: $currentGameState"
                }
                emit(currentGameState)

                controlInputs = merge(
                    controlInputDistinctFlow.drop(1)
                            .onEach { logger.debug { "Game Loop - Control Inputs: $it" } },
                    flow {
                        // Delay to maintain frame rate (60 FPS)
                        delay(sleepTimeMs)
                        emit(controlInputsFlow.firstOrNull() ?: controlInputs)
                    }
                ).first()
            }
        }

    private fun averageFps(deltaTimeMs: Long): Int {
        val fps = if (deltaTimeMs > 0) (1000L / deltaTimeMs).toInt() else 0

        fpsHistory.add(fps)
        if (fpsHistory.size > 60) {
            fpsHistory.removeAt(0)
        }
        return if (fpsHistory.isNotEmpty()) fpsHistory.average().toInt() else 0
    }

    /**
     * Updates the game state based on physics and controls.
     */
    private fun updatedGameState(
        physicsEngine: PhysicsEngine,
        deltaTimeMs: Long,
        fps: Int,
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
            terrain = currentGameState.environment.terrain,
            config = currentGameState.environment.config,
        )

        // Check if lander has landed or crashed
        return when (newLanderState.flightStatus) {
            FlightStatus.CRASHED -> {
                val message = failureMessages.random()
                logger.info("GameState") { "Lander crashed! Message: $message" }
                soundService.playCrashSound()  // Play crash sound
                GameScreenState.GameOver(false, message)
            }
            FlightStatus.LANDED -> {
                val message = successMessages.random()
                logger.info("GameState") { "Lander successfully landed! Message: $message" }
                soundService.playLandingSuccessSound()  // Play success sound
                GameScreenState.GameOver(true, message)
            }
            else -> {
                val camera = Camera.calculateCameraInfo(
                    landerState = newLanderState,
                    config = currentGameState.environment.config
                )
                GameScreenState.Playing(
                    landerState = newLanderState,
                    environment = currentGameState.environment,
                    fps = fps,
                    camera = camera,
                ).also { state ->
                    if (KmLogging.isLoggingDebug) {
                        logger.d("GameState") {
                            "Lander mission active! : cameraZoomLevel=${camera.zoomLevel}, cameraOffset=${camera.offset}, LanderState=${state.landerState}"
                        }
                    } else {
                        logger.v("GameState") { "Lander mission active! : $state" }
                    }
                }
            }
        }
    }

    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        soundService.dispose()
        super.onCleared()
    }

    companion object {
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
            val environment: GameEnvironmentState = GameEnvironmentState(),

            /**
             * Current frames per second.
             */
            val fps: Int = 0,

            val camera: Camera = Camera(),
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
}
