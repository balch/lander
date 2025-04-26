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

    // Collect UI states
    val startScreenState by startScreenViewModel.uiState.collectAsState()
    val gameScreenState by gameViewModel.uiState.collectAsState()

    // Track current screen
    var currentScreen by remember {
        // Start with the Start Screen
        mutableStateOf(Screen.StartScreen)
    }

    // Handle navigation
    when (currentScreen) {
        Screen.StartScreen -> {
            // Show Start Screen
            StartScreen(
                uiState = startScreenState,
                onFuelLevelChanged = startScreenViewModel::updateFuelLevel,
                onGravityLevelChanged = startScreenViewModel::updateGravityLevel,
                onLandingPadSizeChanged = startScreenViewModel::updateLandingPadSize,
                onThrustStrengthChanged = startScreenViewModel::updateThrustStrength,
                onStartGameClicked = {
                    startScreenViewModel.startGame()
                }
            )

            // Navigate to Game Screen if requested
            if (startScreenState.navigateToGame) {
                // Start game with the configured options
                gameViewModel.startGame(startScreenState.gameConfig)

                // Reset navigation flag
                startScreenViewModel.onGameNavigated()

                // Update current screen
                currentScreen = Screen.GameScreen
            }
        }

        Screen.GameScreen -> {
            // Show Game Screen
            GameScreen(
                uiState = gameScreenState,
                onThrustPressed = { gameViewModel.setThrust(true) },
                onThrustReleased = { gameViewModel.setThrust(false) },
                onRotateLeftPressed = { gameViewModel.setRotateLeft(true) },
                onRotateLeftReleased = { gameViewModel.setRotateLeft(false) },
                onRotateRightPressed = { gameViewModel.setRotateRight(true) },
                onRotateRightReleased = { gameViewModel.setRotateRight(false) },
                onRestartClicked = { gameViewModel.restartGame() },
                onBackToStartClicked = { gameViewModel.navigateToStartScreen() }
            )

            // Navigate back to Start Screen if requested
            if (gameScreenState.navigateToStartScreen) {
                // Reset navigation flag
                gameViewModel.onStartScreenNavigated()

                // Update current screen
                currentScreen = Screen.StartScreen
            }
        }
    }
}

/**
 * Enum representing the screens in the application.
 */
enum class Screen {
    StartScreen,
    GameScreen
}
