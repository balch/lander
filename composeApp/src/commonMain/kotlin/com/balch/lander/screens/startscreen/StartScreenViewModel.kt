package com.balch.lander.screens.startscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balch.lander.GameConfig
import com.balch.lander.GravityLevel
import com.balch.lander.LandingPadSize
import com.balch.lander.ThrustStrength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Start Screen.
 * Handles user configuration options and navigation to the game screen.
 */
class StartScreenViewModel : ViewModel() {
    
    // State for the Start Screen
    private val gameConfigFlow = MutableStateFlow(GameConfig())
    val uiState: StateFlow<StartScreenState> =
        gameConfigFlow
            .map { StartScreenState(it) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StartScreenState())

    /**
     * Updates the game configuration.
     */
    fun updateGameConfig(gameConfig: GameConfig) {
        gameConfigFlow.tryEmit(gameConfig)
    }

    /**
     * Updates the fuel level configuration.
     * @param value Fuel level (0.0 to 1.0)
     */
    fun updateFuelLevel(value: Float) {
        gameConfigFlow.tryEmit(gameConfigFlow.value.copy(fuelLevel = value))
    }
    
    /**
     * Updates the gravity level configuration.
     * @param gravityLevel Selected gravity level
     */
    fun updateGravityLevel(gravityLevel: GravityLevel) {
        gameConfigFlow.tryEmit(gameConfigFlow.value.copy(gravity = gravityLevel))
    }
    
    /**
     * Updates the landing pad size configuration.
     * @param landingPadSize Selected landing pad size
     */
    fun updateLandingPadSize(landingPadSize: LandingPadSize) {
        gameConfigFlow.tryEmit(gameConfigFlow.value.copy(landingPadSize = landingPadSize))
    }
    
    /**
     * Updates the thrust strength configuration.
     * @param thrustStrength Selected thrust strength
     */
    fun updateThrustStrength(thrustStrength: ThrustStrength) {
        gameConfigFlow.tryEmit(gameConfigFlow.value.copy(thrustStrength = thrustStrength))
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