package com.balch.lander

import com.balch.lander.core.game.models.Vector2D

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
    val backgoundStarCount: Int = 250,

    /**
     * render size of the lander
     */
    val landerSize: Float = 20f,

    /**
     * Camera zoom configuration for different distances from the ground.
     * Controls how the camera scales and offsets based on lander position.
     */
    val cameraConfig: CameraConfig = CameraConfig(),
)

/**
 * Gravity levels available in the game.
 * Based on Moon gravity with variations.
 */
enum class GravityLevel(val value: Float, val label: String) {
//    VERY_LOW(0.1f, "Very Low"),
    LOW(0.75f, "Low"),
    MEDIUM(1.0f, "Medium"),
    HIGH(2.0f, "High"),
//    VERY_HIGH(2.0f, "Very High")
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
 * Camera zoom levels based on lander's distance from the ground.
 * Each level defines how the camera should behave at different heights.
 */
enum class CameraZoomLevel(
    val distanceThreshold: Float,
    val scale: Vector2D,
    val label: String
) {
    /**
     * Far from ground - minimal zoom, centered view
     */
    FAR(600f, Vector2D(1.0f, 1.0f), "Far"),

    /**
     * Medium distance - moderate zoom, slightly offset view
     */
    MEDIUM(300f, Vector2D(1.5f, 1.5f), "Medium"),

    /**
     * Close to ground - maximum zoom, focused on landing area
     */
    CLOSE(0f, Vector2D(2.0f, 2.0f), "Close")
}

/**
 * Configuration for camera behavior based on lander position.
 * Controls scaling and offset for different zoom levels.
 */
data class CameraConfig(
    /**
     * Base camera scale when far from ground
     */
    val baseScale: Vector2D = Vector2D(1.0f, 1.0f),

    /**
     * Maximum horizontal offset as a percentage of screen width
     */
    val maxHorizontalOffsetPercent: Float = 0.4f,

    /**
     * Maximum vertical offset as a percentage of screen height
     */
    val maxVerticalOffsetPercent: Float = 0.3f,

    /**
     * Zoom levels configuration
     */
    val zoomLevels: List<CameraZoomLevel> = listOf(
        CameraZoomLevel.FAR,
        CameraZoomLevel.MEDIUM,
        CameraZoomLevel.CLOSE
    )
)
