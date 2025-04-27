package com.balch.lander.screens.gamescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balch.lander.GameConfig
import com.balch.lander.core.TimeUtil
import com.balch.lander.screens.gamescreen.gameplay.ControlInputs
import com.balch.lander.screens.gamescreen.gameplay.PhysicsEngine
import com.balch.lander.screens.gamescreen.gameplay.TerrainGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Game Screen.
 * Handles game logic, physics, and state during gameplay.
 */
class GameViewModel(
    private val terrainGenerator: TerrainGenerator
) : ViewModel() {

    // State for the Game Screen
    private val _uiState = MutableStateFlow(GameScreenState())
    val uiState: StateFlow<GameScreenState> = _uiState.asStateFlow()

    // Physics engine
    private var physicsEngine: PhysicsEngine? = null

    // Game loop job
    private var gameLoopJob: Job? = null

    // Last update time
    private var lastUpdateTime = 0L

    // Control inputs
    private val controlInputs = ControlInputs()

    /**
     * Starts a new game with the given configuration.
     * @param config Game configuration
     */
    fun startGame(config: GameConfig) {
        // Create physics engine with the given configuration
        physicsEngine = PhysicsEngine(config)

        // Generate terrain
        val screenWidth = 1000f // Default screen width in game units
        val screenHeight = 1000f // Default screen height in game units
        val terrain = terrainGenerator.generateTerrain(
            width = screenWidth,
            height = screenHeight,
            landingPadSize = config.landingPadSize,
            seed = Random.nextLong()
        )

        // Calculate initial fuel based on config
        val initialFuel = 100f + config.fuelLevel * 100f // 100-200 units of fuel

        // Create initial game state
        val initialState = GameState(
            position = Vector2D(screenWidth / 2, 50f), // Start at top center
            velocity = Vector2D(0f, 0f),
            rotation = 0f,
            fuel = initialFuel,
            initialFuel = initialFuel,
            terrain = terrain,
            config = config
        )

        // Update UI state
        _uiState.update { currentState ->
            currentState.copy(
                gameState = initialState,
                isGameRunning = true,
                successMessages = getSuccessMessages(),
                failureMessages = getFailureMessages()
            )
        }

        // Start game loop
        startGameLoop()
    }

    /**
     * Starts the game loop.
     */
    private fun startGameLoop() {
        // Cancel existing game loop if any
        gameLoopJob?.cancel()

        // Initialize last update time
        lastUpdateTime = TimeUtil.currentTimeMillis()

        // Start new game loop
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                // Calculate delta time
                val currentTime = TimeUtil.currentTimeMillis()
                val deltaTime = (currentTime - lastUpdateTime) / 1000f
                lastUpdateTime = currentTime

                // Update game state
                updateGameState(deltaTime)

                // Delay to maintain frame rate (60 FPS)
                delay(16) // ~60 FPS
            }
        }
    }

    /**
     * Updates the game state based on physics and controls.
     * @param deltaTime Time elapsed since last update in seconds
     */
    private fun updateGameState(deltaTime: Float) {
        val currentState = _uiState.value.gameState
        val physicsEngine = physicsEngine ?: return

        // Skip update if game is not in playing state
        if (currentState.status != GameStatus.PLAYING) {
            return
        }

        // Update game state using physics engine
        val newState = physicsEngine.update(currentState, deltaTime, controlInputs)

        // Check if game status has changed
        val statusChanged = currentState.status != newState.status

        // Calculate FPS from deltaTime
        val currentFps = if (deltaTime > 0) (1f / deltaTime).toInt() else 0

        // Update UI state
        _uiState.update { currentUiState ->
            currentUiState.copy(
                gameState = newState,
                showSuccessMessage = statusChanged && newState.status == GameStatus.LANDED,
                showFailureMessage = statusChanged && newState.status == GameStatus.CRASHED,
                selectedMessage = if (statusChanged) {
                    when (newState.status) {
                        GameStatus.LANDED -> currentUiState.successMessages.random()
                        GameStatus.CRASHED -> currentUiState.failureMessages.random()
                        else -> ""
                    }
                } else {
                    currentUiState.selectedMessage
                },
                fps = currentFps
            )
        }

        // Stop game loop if game is over
        if (statusChanged && newState.status != GameStatus.PLAYING) {
            gameLoopJob?.cancel()
        }
    }

    /**
     * Sets the thrust control input.
     * @param isThrusting Whether thrust is active
     */
    fun setThrust(isThrusting: Boolean) {
        controlInputs.thrust = isThrusting
    }

    /**
     * Sets the rotate left control input.
     * @param isRotatingLeft Whether rotating left is active
     */
    fun setRotateLeft(isRotatingLeft: Boolean) {
        controlInputs.rotateLeft = isRotatingLeft
    }

    /**
     * Sets the rotate right control input.
     * @param isRotatingRight Whether rotating right is active
     */
    fun setRotateRight(isRotatingRight: Boolean) {
        controlInputs.rotateRight = isRotatingRight
    }

    /**
     * Restarts the game with the same configuration.
     */
    fun restartGame() {
        val config = _uiState.value.gameState.config
        startGame(config)
    }

    /**
     * Navigates back to the start screen.
     */
    fun navigateToStartScreen() {
        // Cancel game loop
        gameLoopJob?.cancel()

        // Update UI state
        _uiState.update { currentState ->
            currentState.copy(
                isGameRunning = false,
                navigateToStartScreen = true
            )
        }
    }

    /**
     * Resets the navigation flag after navigation is handled.
     */
    fun onStartScreenNavigated() {
        _uiState.update { currentState ->
            currentState.copy(
                navigateToStartScreen = false
            )
        }
    }

    /**
     * Gets a list of success messages.
     */
    private fun getSuccessMessages(): List<String> {
        return listOf(
            "Perfect landing! NASA would be proud.",
            "Touchdown! The Eagle has landed.",
            "Smooth as silk! You're a natural.",
            "Landing confirmed. Mission accomplished!",
            "That's one small step for a lander...",
            "Houston, we have a successful landing!"
        )
    }

    /**
     * Gets a list of failure messages.
     */
    private fun getFailureMessages(): List<String> {
        return listOf(
            "Houston, we have a problem.",
            "That's going to leave a mark...",
            "The lander is now a permanent lunar feature.",
            "Let's call that a 'rapid unscheduled disassembly'.",
            "Maybe try landing gear down next time?",
            "The moon claims another victim."
        )
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}

/**
 * UI state for the Game Screen.
 */
data class GameScreenState(
    /**
     * Current game state.
     */
    val gameState: GameState = GameState(),

    /**
     * Whether the game is currently running.
     */
    val isGameRunning: Boolean = false,

    /**
     * Whether to navigate back to the start screen.
     */
    val navigateToStartScreen: Boolean = false,

    /**
     * Whether to show the success message.
     */
    val showSuccessMessage: Boolean = false,

    /**
     * Whether to show the failure message.
     */
    val showFailureMessage: Boolean = false,

    /**
     * List of success messages.
     */
    val successMessages: List<String> = emptyList(),

    /**
     * List of failure messages.
     */
    val failureMessages: List<String> = emptyList(),

    /**
     * Currently selected message to display.
     */
    val selectedMessage: String = "",

    /**
     * Current frames per second.
     */
    val fps: Int = 0
)
