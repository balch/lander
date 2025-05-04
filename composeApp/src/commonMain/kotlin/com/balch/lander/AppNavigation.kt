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
    // Inject ViewModels
    val startViewModel = koinInject<StartViewModel>()
    val gamePlayViewModel = koinInject<GamePlayViewModel>()

    // Track current screen using the sealed interface Screen
    var currentScreen: Screen by remember {
        // Start with the Start Screen
        mutableStateOf(Screen.StartScreen())
    }

    // Handle navigation based on current screen
    when (currentScreen) {
        is Screen.StartScreen -> {
            // Collect UI state only when on this screen for better performance
            val startScreenState by startViewModel.uiState.collectAsState()
            StartScreen(
                uiState = startScreenState,
                onFuelLevelChanged = startViewModel::updateFuelLevel,
                onGravityLevelChanged = startViewModel::updateGravityLevel,
                onLandingPadSizeChanged = startViewModel::updateLandingPadSize,
                onStartGameClicked = {
                    // Direct navigation to GameScreen with config passed as parameter
                    currentScreen = Screen.GameScreen(startViewModel.uiState.value.gameConfig)
                }
            )
            // Use LaunchedEffect to start the game when this screen is shown or config changes
            LaunchedEffect(currentScreen.config) {
                startViewModel.updateGameConfig(currentScreen.config)
            }
        }

        is Screen.GameScreen -> {
            val gameScreenState by gamePlayViewModel.uiState.collectAsState()
            GamePlayScreen(
                uiState = gameScreenState,
                onControlInputs = gamePlayViewModel::setControlsInputs,
                onRestartClicked = { gamePlayViewModel.startGame(currentScreen.config) },
                onBackToStartClicked = { currentScreen = Screen.StartScreen(currentScreen.config) }
            )

            // Use LaunchedEffect to start the game when this screen is shown or config changes
            LaunchedEffect(currentScreen.config) {
                gamePlayViewModel.startGame(currentScreen.config)
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
