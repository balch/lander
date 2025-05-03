package com.balch.lander.screens.gamescreen

import com.balch.lander.GameConfig
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D
import kotlin.math.abs

/**
 * Constants for safe landing conditions
 */
private const val MAX_SAFE_LANDING_VELOCITY_Y = 2.0f
private const val MAX_SAFE_LANDING_VELOCITY_X = 1.0f
private const val MAX_SAFE_LANDING_ANGLE = 15.0f

/**
 * Represents the dynamic state of the lander that changes with the game loop.
 */
data class LanderState(
    /**
     * Current position of the lander (x, y).
     * Origin (0,0) is at the top-left corner of the screen.
     */
    val position: Vector2D = Vector2D(0f, 0f),

    /**
     * Distance from the lander to the ground directly below.
     */
    val distanceToGround: Float = Float.POSITIVE_INFINITY,

    /**
     * Whether the lander is currently thrusting.
     */
    val thrustStrength: ThrustStrength = ThrustStrength.OFF,

    /**
     * Current velocity of the lander (x, y) in units per second.
     */
    val velocity: Vector2D = Vector2D(0f, 0f),

    /**
     * Whether the lander is in danger of crashing.
     */
    val isDangerMode: Boolean = false,

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
     * Current game status.
     */
    val status: GameStatus = GameStatus.PLAYING,
) {
    /**
     * Checks if the lander has landed successfully.
     * Conditions for successful landing:
     * 1. Lander is on a landing pad
     * 2. Vertical velocity is low
     * 3. Horizontal velocity is low
     * 4. Lander is relatively upright
     * 
     * @param terrain The terrain to check landing pad collision against
     * @return true if the lander has landed successfully, false otherwise
     */
    fun hasLandedSuccessfully(terrain: Terrain): Boolean {
        val isOnLandingPad = terrain.isOnLandingPad(position.x)
        val isVerticalVelocitySafe = abs(velocity.y) < MAX_SAFE_LANDING_VELOCITY_Y
        val isHorizontalVelocitySafe = abs(velocity.x) < MAX_SAFE_LANDING_VELOCITY_X
        val isUprightEnough = abs(rotation) < MAX_SAFE_LANDING_ANGLE
        val hasReachedGround = distanceToGround == 0F

        return isOnLandingPad
                && isVerticalVelocitySafe
                && isHorizontalVelocitySafe
                && isUprightEnough
                && hasReachedGround
    }

    /**
     * Checks if the lander has crashed.
     * Conditions for crash:
     * 1. Lander has hit the ground
     * 2. And has not landed successfully
     * 
     * @param terrain The terrain to check ground collision against
     * @return true if the lander has crashed, false otherwise
     */
    fun hasCrashed(terrain: Terrain): Boolean {
        val hasHitGround = distanceToGround <= 0F
        return hasHitGround
                && !hasLandedSuccessfully(terrain)
    }
}

/**
 * Represents the static state of the game environment that is generated at game start.
 */
data class GameEnvironmentState(
    /**
     * Current terrain configuration.
     */
    val terrain: Terrain = Terrain(),

    /**
     * Current game configuration.
     */
    val config: GameConfig = GameConfig()
)

/**
 * Represents the current status of the game.
 */
enum class GameStatus {
    PLAYING,
    LANDED,
    CRASHED
}
