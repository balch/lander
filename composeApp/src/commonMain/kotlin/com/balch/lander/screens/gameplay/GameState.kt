package com.balch.lander.screens.gameplay

import androidx.compose.ui.graphics.Color
import com.balch.lander.GameConfig
import com.balch.lander.Platform
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D

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
     * Distance from the lander to the min terrain point.
     */
    val distanceToSeaLevel: Float = Float.POSITIVE_INFINITY,

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
    val flightStatus: FlightStatus = FlightStatus.NOMINAL,

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
)

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
    val config: GameConfig = GameConfig(),

    /**
     * Current platform configuration.
     */
    val platform: Platform = Platform()
)

enum class FlightStatus(val color: Color) {
    NOMINAL(Color.White),
    WARNING(Color.Yellow),
    DANGER(Color.Red),
    ALIGNED(Color.Green),
    LANDED(Color.Green),
    CRASHED(Color.Red),
}
fun LanderState.isGameOver() =
    flightStatus == FlightStatus.CRASHED || flightStatus == FlightStatus.LANDED