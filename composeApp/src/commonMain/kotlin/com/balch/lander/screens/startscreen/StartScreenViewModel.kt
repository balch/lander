package com.balch.lander.screens.startscreen

import androidx.lifecycle.ViewModel
import com.balch.lander.GameConfig
import com.balch.lander.GravityLevel
import com.balch.lander.LandingPadSize
import com.balch.lander.core.coroutines.CoroutineScopeProvider
import com.balch.lander.core.coroutines.DispatcherProvider
import kotlinx.coroutines.flow.*
import org.lighthousegames.logging.KmLog

/**
 * ViewModel for the Start Screen.
 * Handles user configuration options and navigation to the game screen.
 */
class StartScreenViewModel(
    dispatcherProvider: DispatcherProvider,
    scopeProvider: CoroutineScopeProvider,
    private val logger: KmLog
) : ViewModel() {

    // State for the Start Screen
    private val gameConfigFlow = MutableStateFlow(GameConfig())
    val uiState: StateFlow<StartScreenState> =
        gameConfigFlow
            .map { StartScreenState(it) }
            .flowOn(dispatcherProvider.default)
            .stateIn(
                scope = scopeProvider[this],
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = StartScreenState()
            )

    /**
     * Updates the game configuration.
     */
    fun updateGameConfig(gameConfig: GameConfig) {
        logger.info { "Updating game config: gravity=${gameConfig.gravity}, fuelLevel=${gameConfig.fuelLevel}, landingPadSize=${gameConfig.landingPadSize}" }
        gameConfigFlow.tryEmit(gameConfig)
    }

    /**
     * Updates the fuel level configuration.
     * @param value Fuel level (0.0 to 1.0)
     */
    fun updateFuelLevel(value: Float) {
        logger.debug { "Updating fuel level: $value" }
        gameConfigFlow.tryEmit(gameConfigFlow.value.copy(fuelLevel = value))
    }

    /**
     * Updates the gravity level configuration.
     * @param gravityLevel Selected gravity level
     */
    fun updateGravityLevel(gravityLevel: GravityLevel) {
        logger.debug { "Updating gravity level: $gravityLevel" }
        gameConfigFlow.tryEmit(gameConfigFlow.value.copy(gravity = gravityLevel))
    }

    /**
     * Updates the landing pad size configuration.
     * @param landingPadSize Selected landing pad size
     */
    fun updateLandingPadSize(landingPadSize: LandingPadSize) {
        logger.debug { "Updating landing pad size: $landingPadSize" }
        gameConfigFlow.tryEmit(gameConfigFlow.value.copy(landingPadSize = landingPadSize))
    }
}

/**
 * UI state for the Start Screen.
 */
data class StartScreenState(
    /**
     * Current game configuration.
     */
    val gameConfig: GameConfig = GameConfig(),
)
