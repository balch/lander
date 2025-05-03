package com.balch.lander.core.game.models

/**
 * Represents the terrain of the moon, including landing pads.
 */
class Terrain(
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
     * Uses linear interpolation between terrain points.
     */
    fun getGroundHeight(x: Float): Float {
        // If there are no points, return 0
        if (points.isEmpty()) {
            return 0f
        }

        // Check if x is before the first point
        if (x <= points.first().x) {
            return points.first().y
        }

        // Check if x is after the last point
        if (x >= points.last().x) {
            return points.last().y
        }

        // Find the two points that x is between
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]

            if (x >= p1.x && x <= p2.x) {
                // Linear interpolation between the two points
                val t = (x - p1.x) / (p2.x - p1.x)
                return p1.y + t * (p2.y - p1.y)
            }
        }

        // Fallback
        return points.last().y
    }

    /**
     * Checks if the given x coordinate is on a landing pad.
     */
    fun isOnLandingPad(x: Float): Boolean =
        landingPads.any { pad -> x >= pad.start.x && x <= pad.end.x }

    override fun toString(): String =
        "Terrain(points=${points.size}, landingPads=${landingPads.size})"
}