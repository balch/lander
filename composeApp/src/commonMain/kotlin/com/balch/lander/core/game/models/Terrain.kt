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
    val landingPads: List<LandingPad> = emptyList(),
) {
    /**
     * Gets the height of the ground at the given x coordinate.
     * Uses linear interpolation between terrain points.
     */
    fun getGroundHeight(x: Float): Float =
        when {
            points.isEmpty() -> 0f
            x <= points.first().x -> points.first().y
            x >= points.last().x -> points.last().y
            else -> {
                var y: Float? = null
                // Find the two points that x is between
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]

                    if (x >= p1.x && x <= p2.x) {
                        // Linear interpolation between the two points
                        val t = (x - p1.x) / (p2.x - p1.x)
                        y = p1.y + t * (p2.y - p1.y)
                        break
                    }
                }
                y ?:points.last().y
            }
        }

    /**
     * Represents the sea level height relative to the terrain.
     *
     * The sea level is calculated as the highest y-coordinate among the terrain points,
     * increased by 50 units. If no terrain points exist, the sea level defaults to 0.
     */
    val seaLevel: Float =
        (points.maxByOrNull { it.y }?.y)?.plus(50) ?: 0f

    /**
     * Checks if the given x coordinate is on a landing pad.
     */
    fun isOnLandingPad(x: Float): Boolean =
        landingPads.any { pad -> x >= pad.start.x && x <= pad.end.x }

    override fun toString(): String =
        "Terrain(points=${points.size}, landingPads=${landingPads.size})"
}