package com.balch.lander.core.game.models

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