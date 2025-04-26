package com.balch.lander

/**
 * Configuration options for the Lunar Lander game.
 * These settings can be adjusted by the user on the start screen.
 */
data class GameConfig(
    /**
     * Amount of fuel available for the lander.
     * Range: 0.0 (low) to 1.0 (high)
     */
    val fuelLevel: Float = 0.5f,
    
    /**
     * Gravity strength affecting the lander.
     * Based on Moon gravity with 5 options.
     */
    val gravity: GravityLevel = GravityLevel.MEDIUM,
    
    /**
     * Size of the landing pad.
     * 3 options available.
     */
    val landingPadSize: LandingPadSize = LandingPadSize.MEDIUM,
    
    /**
     * Strength of the lander's thrust.
     * 3 options available.
     */
    val thrustStrength: ThrustStrength = ThrustStrength.MEDIUM
)

/**
 * Gravity levels available in the game.
 * Based on Moon gravity with variations.
 */
enum class GravityLevel(val value: Float, val label: String) {
    VERY_LOW(0.5f, "Very Low"),
    LOW(0.8f, "Low"),
    MEDIUM(1.0f, "Medium"),
    HIGH(1.2f, "High"),
    VERY_HIGH(1.5f, "Very High")
}

/**
 * Landing pad sizes available in the game.
 */
enum class LandingPadSize(val value: Float, val label: String) {
    SMALL(0.5f, "Small"),
    MEDIUM(1.0f, "Medium"),
    LARGE(1.5f, "Large")
}

/**
 * Thrust strength options available in the game.
 */
enum class ThrustStrength(val value: Float, val label: String) {
    LOW(0.8f, "Low"),
    MEDIUM(1.0f, "Medium"),
    HIGH(1.2f, "High")
}