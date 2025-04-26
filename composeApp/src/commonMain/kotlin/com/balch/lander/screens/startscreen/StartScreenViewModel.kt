package com.balch.lander.screens.startscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balch.lander.GameConfig
import com.balch.lander.GravityLevel
import com.balch.lander.LandingPadSize
import com.balch.lander.ThrustStrength
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Start Screen.
 * Handles user configuration options and navigation to the game screen.
 */
class StartScreenViewModel : ViewModel() {
    
    // State for the Start Screen
    private val _uiState = MutableStateFlow(StartScreenState())
    val uiState: StateFlow<StartScreenState> = _uiState.asStateFlow()
    
    /**
     * Updates the fuel level configuration.
     * @param value Fuel level (0.0 to 1.0)
     */
    fun updateFuelLevel(value: Float) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    gameConfig = currentState.gameConfig.copy(
                        fuelLevel = value
                    )
                )
            }
        }
    }
    
    /**
     * Updates the gravity level configuration.
     * @param gravityLevel Selected gravity level
     */
    fun updateGravityLevel(gravityLevel: GravityLevel) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    gameConfig = currentState.gameConfig.copy(
                        gravity = gravityLevel
                    )
                )
            }
        }
    }
    
    /**
     * Updates the landing pad size configuration.
     * @param landingPadSize Selected landing pad size
     */
    fun updateLandingPadSize(landingPadSize: LandingPadSize) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    gameConfig = currentState.gameConfig.copy(
                        landingPadSize = landingPadSize
                    )
                )
            }
        }
    }
    
    /**
     * Updates the thrust strength configuration.
     * @param thrustStrength Selected thrust strength
     */
    fun updateThrustStrength(thrustStrength: ThrustStrength) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    gameConfig = currentState.gameConfig.copy(
                        thrustStrength = thrustStrength
                    )
                )
            }
        }
    }
    
    /**
     * Starts the game with the current configuration.
     */
    fun startGame() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    navigateToGame = true
                )
            }
        }
    }
    
    /**
     * Resets the navigation flag after navigation is handled.
     */
    fun onGameNavigated() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    navigateToGame = false
                )
            }
        }
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
    
    /**
     * Whether to navigate to the game screen.
     */
    val navigateToGame: Boolean = false
)