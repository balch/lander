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
     * Virtual screen width used for game calculations.
     * This provides a consistent coordinate system regardless of actual screen size.
     */
    val screenWidth: Float = 1000f,

    /**
     * Virtual screen height used for game calculations.
     * This provides a consistent coordinate system regardless of actual screen size.
     */
    val screenHeight: Float = 1000f,

    /**
     * Number of stars to display in the background.
     * Controls the density of stars in the space backdrop.
     */
    val backgoundStarCount: Int = 250
)

/**
 * Gravity levels available in the game.
 * Based on Moon gravity with variations.
 */
enum class GravityLevel(val value: Float, val label: String) {
    VERY_LOW(0.1f, "Very Low"),
    LOW(0.8f, "Low"),
    MEDIUM(1.0f, "Medium"),
    HIGH(1.2f, "High"),
    VERY_HIGH(2.0f, "Very High")
}

/**
 * Landing pad sizes available in the game.
 */
enum class LandingPadSize(val value: Float, val label: String) {
    SMALL(0.5f, "Small"),
    MEDIUM(1.0f, "Medium"),
    LARGE(1.5f, "Large")
}
