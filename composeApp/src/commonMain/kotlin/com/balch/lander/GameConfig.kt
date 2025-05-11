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

    /**
     * Config to represent danger zone criteria
     */
    val dangerZoneConfig: DangerZoneConfig = DangerZoneConfig(),

    val safeLandingConfig: SafeLandingConfig = SafeLandingConfig(),
) {
    val landerOffset = landerSize / 2f
}

/**
 * Configuration for defining operational parameters in a "danger zone" scenario.
 *
 * @property lowFuel Defines the remaining fuel threshold (in percentage) below which the system considers fuel dangerously low.
 * @property velocityThreshold Specifies the velocity (in units per second) above which the system considers movement dangerously fast.
 * @property distanceToGround Represents the proximity to the ground (in units) under which the system treats the situation as dangerous.
 */
data class DangerZoneConfig(
    val lowFuel: Int = 20,
    val velocityThreshold: Float = 15f,
    val distanceToGround: Int = 75,
)

/**
 * Configuration class for determining safe landing parameters in the game.
 *
 * Defines the thresholds for vertical velocity and rotation angle that are considered safe
 * during a landing operation. These parameters are used to evaluate the success of a landing
 * and determine if the player has adhered to the acceptable landing criteria.
 *
 * @property velocityThreshold The maximum allowable vertical velocity (in units per second) for a safe landing.
 * @property rotationThreshold The maximum allowable rotational angle (in degrees) for a safe landing.
 */
data class SafeLandingConfig(
    val velocityThreshold: Float = 4f,
    val rotationThreshold: Float = 4f,
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
enum class LandingPadSize(val size: Float, val count: Int, val label: String) {
    SMALL(0.5f, 6, "Small"),
    MEDIUM(1.0f, 4,  "Medium"),
    LARGE(1.5f, 2, "Large")
}

/**
 * Camera zoom levels based on lander's distance from the ground.
 * Each level defines how the camera should behave at different heights.
 */
enum class CameraZoomLevel(
    val distanceThreshold: Float,
    val scale: Float,
    val screenOffsetMultiplier: Float,
) {
    /**
     * Far from ground - minimal zoom, centered view
     */
    FAR(600f, 1.0f, 0f),

    /**
     * Medium distance - moderate zoom, slightly offset view
     */
    MEDIUM(400f, 1.25f, .125f),

    /**
     * Close to ground - maximum zoom, focused on landing area
     */
    CLOSE(0f, 2f, .2f)
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
    val maxHorizontalOffsetPercent: Float = 0.2f,

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
