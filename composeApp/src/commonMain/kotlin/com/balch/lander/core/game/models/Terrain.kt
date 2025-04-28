package com.balch.lander.core.game.models

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