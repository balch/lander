package com.balch.lander

import androidx.compose.runtime.*
import com.balch.lander.screens.gameplay.GamePlayScreen
import com.balch.lander.screens.gameplay.GamePlayViewModel
import com.balch.lander.screens.start.StartScreen
import com.balch.lander.screens.start.StartViewModel
import org.koin.compose.koinInject

/**
 * Navigation component for the Lunar Lander application.
 * Handles navigation between the Start Screen and Game Screen.
 */
@Composable
fun AppNavigation() {
    // Track current screen using the sealed interface Screen
    var currentScreen: Screen by remember {
        // Start with the Start Screen
        mutableStateOf(Screen.StartScreen())
    }

    // Handle navigation based on current screen
    when (currentScreen) {
        is Screen.StartScreen -> {
            StartScreenLauncher(
                gameConfig = currentScreen.config,
                onStartGameClicked = { gameConfig ->
                    currentScreen = Screen.GameScreen(gameConfig)
                }
            )
        }

        is Screen.GameScreen -> {
            GameScreenLauncher(
                gameConfig = currentScreen.config,
                onBackToStartClicked = { currentScreen = Screen.StartScreen(currentScreen.config) }
            )
        }
    }
}

@Composable
fun GameScreenLauncher(
    gameConfig: GameConfig,
    onBackToStartClicked: () -> Unit,
) {
    val gamePlayViewModel = koinInject<GamePlayViewModel>()

    val gameScreenState by gamePlayViewModel.uiState.collectAsState()
    GamePlayScreen(
        uiState = gameScreenState,
        onControlInputs = gamePlayViewModel::setControlsInputs,
        onRestartClicked = { gamePlayViewModel.startGame(gameConfig) },
        onBackToStartClicked = onBackToStartClicked
    )

    // Use LaunchedEffect to start the game when this screen is shown or config changes
    LaunchedEffect(gameConfig) {
        gamePlayViewModel.startGame(gameConfig)
    }
}

@Composable
fun StartScreenLauncher(
    gameConfig: GameConfig,
    onStartGameClicked: (GameConfig) -> Unit,
) {
    val startViewModel = koinInject<StartViewModel>()

    // Collect UI state only when on this screen for better performance
    val startScreenState by startViewModel.uiState.collectAsState()
    StartScreen(
        uiState = startScreenState,
        onFuelLevelChanged = startViewModel::updateFuelLevel,
        onGravityLevelChanged = startViewModel::updateGravityLevel,
        onLandingPadSizeChanged = startViewModel::updateLandingPadSize,
        onStartGameClicked = { onStartGameClicked(startViewModel.uiState.value.gameConfig) }
    )
    // Use LaunchedEffect to start the game when this screen is shown or config changes
    LaunchedEffect(gameConfig) {
        startViewModel.updateGameConfig(gameConfig)
    }
}

/**
 * Sealed interface representing the screens in the application.
 */
sealed interface Screen {
    val config: GameConfig

    /**
     * Start screen with game configuration options
     */
    data class StartScreen(override val config: GameConfig = GameConfig()): Screen

    /**
     * Game screen that requires game configuration
     * @param config The game configuration to use for this game session
     */
    data class GameScreen(override val config: GameConfig): Screen
}
