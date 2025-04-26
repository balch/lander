package com.balch.lander.model

import kotlin.math.abs

/**
 * Represents the current state of the Lunar Lander game.
 */
data class GameState(
    /**
     * Current position of the lander (x, y).
     * Origin (0,0) is at the top-left corner of the screen.
     */
    val position: Vector2D = Vector2D(0f, 0f),

    /**
     * Current velocity of the lander (x, y) in units per second.
     */
    val velocity: Vector2D = Vector2D(0f, 0f),

    /**
     * Current rotation of the lander in degrees.
     * 0 degrees is pointing upward.
     */
    val rotation: Float = 0f,

    /**
     * Current amount of fuel remaining.
     * Range: 0.0 to initialFuel
     */
    val fuel: Float = 100f,

    /**
     * Initial amount of fuel.
     */
    val initialFuel: Float = 100f,

    /**
     * Whether the lander is currently thrusting.
     */
    val isThrusting: Boolean = false,

    /**
     * Current game status.
     */
    val status: GameStatus = GameStatus.PLAYING,

    /**
     * Current terrain configuration.
     */
    val terrain: Terrain = Terrain(),

    /**
     * Current game configuration.
     */
    val config: GameConfig = GameConfig(),

    /**
     * Distance from the lander to the ground directly below.
     */
    val distanceToGround: Float = 0f,

    /**
     * Whether the lander is in danger of crashing.
     */
    val isDangerMode: Boolean = false
) {
    /**
     * Checks if the lander has landed successfully.
     * Conditions for successful landing:
     * 1. Lander is on a landing pad
     * 2. Vertical velocity is low
     * 3. Horizontal velocity is low
     * 4. Lander is relatively upright
     */
    fun hasLandedSuccessfully(): Boolean {
        val isOnLandingPad = terrain.isOnLandingPad(position.x)
        val isVerticalVelocitySafe = abs(velocity.y) < MAX_SAFE_LANDING_VELOCITY_Y
        val isHorizontalVelocitySafe = abs(velocity.x) < MAX_SAFE_LANDING_VELOCITY_X
        val isUprightEnough = abs(rotation) < MAX_SAFE_LANDING_ANGLE

        return isOnLandingPad && isVerticalVelocitySafe && isHorizontalVelocitySafe && isUprightEnough
    }

    /**
     * Checks if the lander has crashed.
     * Conditions for crash:
     * 1. Lander has hit the ground
     * 2. And has not landed successfully
     */
    fun hasCrashed(): Boolean {
        val hasHitGround = position.y >= terrain.getGroundHeight(position.x)
        return hasHitGround && !hasLandedSuccessfully()
    }

    companion object {
        const val MAX_SAFE_LANDING_VELOCITY_Y = 2.0f
        const val MAX_SAFE_LANDING_VELOCITY_X = 1.0f
        const val MAX_SAFE_LANDING_ANGLE = 15.0f
    }
}

/**
 * Represents a 2D vector with x and y components.
 */
data class Vector2D(val x: Float, val y: Float) {
    operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2D = Vector2D(x * scalar, y * scalar)
}

/**
 * Represents the current status of the game.
 */
enum class GameStatus {
    PLAYING,
    LANDED,
    CRASHED
}

/**
 * Represents the terrain of the moon, including landing pads.
 */
open class Terrain(
    /**
     * List of terrain points (x, y) that define the surface.
     */
    val points: List<Vector2D> = emptyList(),

    /**
     * List of landing pad segments.
     */
    val landingPads: List<LandingPad> = emptyList()
) {
    /**
     * Gets the height of the ground at the given x coordinate.
     */
    open fun getGroundHeight(x: Float): Float {
        // Default implementation - will be replaced with actual terrain logic
        return 0f
    }

    /**
     * Checks if the given x coordinate is on a landing pad.
     */
    open fun isOnLandingPad(x: Float): Boolean {
        return landingPads.any { pad -> x >= pad.start.x && x <= pad.end.x }
    }
}

/**
 * Represents a landing pad on the terrain.
 */
data class LandingPad(
    /**
     * Start point of the landing pad.
     */
    val start: Vector2D,

    /**
     * End point of the landing pad.
     */
    val end: Vector2D
)
