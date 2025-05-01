package com.balch.lander

import androidx.compose.runtime.*
import com.balch.lander.screens.gamescreen.GameScreen
import com.balch.lander.screens.gamescreen.GameViewModel
import com.balch.lander.screens.startscreen.StartScreen
import com.balch.lander.screens.startscreen.StartScreenViewModel
import org.koin.compose.koinInject

/**
 * Navigation component for the Lunar Lander application.
 * Handles navigation between the Start Screen and Game Screen.
 */
@Composable
fun AppNavigation() {
    // Inject ViewModels
    val startScreenViewModel = koinInject<StartScreenViewModel>()
    val gameViewModel = koinInject<GameViewModel>()

    // Track current screen using the sealed interface Screen
    var currentScreen: Screen by remember {
        // Start with the Start Screen
        mutableStateOf(Screen.StartScreen())
    }

    // Handle navigation based on current screen
    when (currentScreen) {
        is Screen.StartScreen -> {
            // Collect UI state only when on this screen for better performance
            val startScreenState by startScreenViewModel.uiState.collectAsState()
            StartScreen(
                uiState = startScreenState,
                onFuelLevelChanged = startScreenViewModel::updateFuelLevel,
                onGravityLevelChanged = startScreenViewModel::updateGravityLevel,
                onLandingPadSizeChanged = startScreenViewModel::updateLandingPadSize,
                onStartGameClicked = {
                    // Direct navigation to GameScreen with config passed as parameter
                    currentScreen = Screen.GameScreen(startScreenViewModel.uiState.value.gameConfig)
                }
            )
            // Use LaunchedEffect to start the game when this screen is shown or config changes
            LaunchedEffect(currentScreen.config) {
                startScreenViewModel.updateGameConfig(currentScreen.config)
            }
        }

        is Screen.GameScreen -> {
            val gameScreenState by gameViewModel.uiState.collectAsState()
            GameScreen(
                uiState = gameScreenState,
                onControlInputs = gameViewModel::setControlsInputs,
                onRestartClicked = { gameViewModel.startGame(currentScreen.config) },
                onBackToStartClicked = { currentScreen = Screen.StartScreen(currentScreen.config) }
            )

            // Use LaunchedEffect to start the game when this screen is shown or config changes
            LaunchedEffect(currentScreen.config) {
                gameViewModel.startGame(currentScreen.config)
            }
        }
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
