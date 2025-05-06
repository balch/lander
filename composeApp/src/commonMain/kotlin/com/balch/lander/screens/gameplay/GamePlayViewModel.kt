package com.balch.lander.screens.gameplay

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.balch.lander.CameraZoomLevel
import com.balch.lander.GameConfig
import com.balch.lander.core.coroutines.CoroutineScopeProvider
import com.balch.lander.core.coroutines.DispatcherProvider
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.PhysicsEngine
import com.balch.lander.core.game.TerrainGenerator
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.utils.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.lighthousegames.logging.KmLogging
import org.lighthousegames.logging.logging
import kotlin.math.abs

/**
 * ViewModel for the Game Screen.
 * Handles game logic, physics, and state during gameplay.
 */
class GamePlayViewModel(
    private val terrainGenerator: TerrainGenerator,
    private val timeProvider: TimeProvider,
    dispatcherProvider: DispatcherProvider,
    scopeProvider: CoroutineScopeProvider,
) : ViewModel() {

    private val logger = logging()

    // State flows for different components
    private val startGameIntentFlow = MutableSharedFlow<GameConfig>(
        replay = 1, extraBufferCapacity = 1,
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
        replay = 1, extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    ).also { flow ->
        // initialize with empty inputs so we can use
        // .drop(1) when we observe controlInputsFlow to interrupt the
        // game fps delay
        flow.tryEmit(ControlInputs())
    }

    /**
     * Sets the control inputs.
     */
    fun setControlsInputs(controlInputs: ControlInputs) {
        controlInputsFlow.tryEmit(controlInputs)
    }

    // UI state derived from game state and UI state flow
    @OptIn(ExperimentalCoroutinesApi::class)
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
            .flowOn(dispatcherProvider.default)
            .stateIn(
                scope = scopeProvider[this],
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

    /**
     * Calculates the camera scale based on the lander's distance from the ground.
     * As the lander gets closer to the ground, the camera zooms in.
     *
     * @param landerState Current state of the lander
     * @param config Game configuration
     * @return Vector2D representing the scale factor for x and y dimensions
     */
    @VisibleForTesting
    fun calculateCameraZoomLevel(
        landerState: LanderState,
        config: GameConfig
    ): CameraZoomLevel {
        val distance = landerState.distanceToSeaLevel

        // Find the appropriate zoom level based on distance
        val zoomLevel = config.cameraConfig.zoomLevels.find {
            distance >= it.distanceThreshold
        } ?: config.cameraConfig.zoomLevels.first()

        return zoomLevel
    }

    /**
     * Calculates the camera offset based on the lander's position.
     * Adjusts the view to keep the lander in frame as it moves.
     *
     * @param landerState Current state of the lander
     * @param config Game configuration
     * @return Vector2D representing the offset in x and y dimensions (in game coordinates)
     */
    @VisibleForTesting
    fun calculateCameraOffset(
        landerState: LanderState,
        zoomLevel: CameraZoomLevel,
        config: GameConfig
    ): Vector2D {

        // If camera is zoomed out FAR, return (0,0) offset
        if (zoomLevel == CameraZoomLevel.FAR) {
            return Vector2D(0f, 0f)
        }

        // Calculate horizontal offset based on lander's position
        // As lander moves toward edges, camera follows to keep it within 20% of the border
        val horizontalCenter = config.screenWidth / 2
        val borderMargin = config.screenWidth * 0.2f // 20% of screen width
        val maxHorizontalOffset = config.screenWidth * config.cameraConfig.maxHorizontalOffsetPercent

        // Calculate how far from center the lander is
        val horizontalDistanceFromCenter = landerState.position.x - horizontalCenter

        // Only start offsetting when lander is beyond the border margin
        val horizontalOffsetFactor = if (abs(horizontalDistanceFromCenter) > borderMargin) {
            // Normalize to -1..1 range, accounting for the border margin
            val adjustedDistance = horizontalDistanceFromCenter - (if (horizontalDistanceFromCenter > 0) borderMargin else -borderMargin)
            val normalizedDistance = adjustedDistance / (horizontalCenter - borderMargin)
            normalizedDistance.coerceIn(-1f, 1f)
        } else {
            0f
        }

        // Calculate vertical offset based on zoom level
        val verticalOffset = config.screenWidth * zoomLevel.screenOffsetMultiplier

        return Vector2D(
            x = maxHorizontalOffset * horizontalOffsetFactor,
            y = verticalOffset
        )
    }

    /**
     * Updates the game state based on physics and controls.
     * @param  deltaTimeMs Time elapsed since last update in seconds
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
            GameStatus.PLAYING -> {
                val cameraZoomLevel = calculateCameraZoomLevel(
                    landerState = newLanderState,
                    config = currentGameState.environmentState.config
                )
                val cameraOffset = calculateCameraOffset(
                    landerState = newLanderState,
                    zoomLevel = cameraZoomLevel,
                    config = currentGameState.environmentState.config
                )
                GameScreenState.Playing(
                    landerState = newLanderState,
                    environmentState = currentGameState.environmentState,
                    fps = if (deltaTimeMs > 0) (1000L / deltaTimeMs).toInt() else 0,
                    cameraScale = cameraZoomLevel.scale,
                    cameraOffset = cameraOffset,
                ).also { state ->
                    if (KmLogging.isLoggingDebug) {
                        logger.d("GameState") {
                            "Lander mission active! : cameraZoomLevel=${cameraZoomLevel}, cameraOffset=$cameraOffset, LanderState=${state.landerState}"
                        }
                    } else {
                        logger.v("GameState") { "Lander mission active! : $state" }
                    }
                }
            }

            GameStatus.LANDED -> {
                val message = successMessages.random()
                logger.info("GameState") { "Lander successfully landed! Message: $message" }
                GameScreenState.GameOver(true, message)
            }

            GameStatus.CRASHED -> {
                val message = failureMessages.random()
                logger.info("GameState") { "Lander crashed! Message: $message" }
                GameScreenState.GameOver(false, message)
            }
        }
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
            val environmentState: GameEnvironmentState = GameEnvironmentState(),

            /**
             * Current frames per second.
             */
            val fps: Int = 0,

            val cameraScale: Float = 1f,

            val cameraOffset: Vector2D = Vector2D(0f, 0f),
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
