package com.balch.lander.model

import kotlin.random.Random

/**
 * Generates random terrain for the Lunar Lander game.
 */
class TerrainGenerator {
    /**
     * Generates random terrain with landing pads.
     * 
     * @param width Width of the terrain in game units
     * @param height Height of the terrain in game units
     * @param landingPadSize Size of the landing pads
     * @param numLandingPads Number of landing pads to generate
     * @param seed Random seed for terrain generation
     * @return Generated terrain
     */
    fun generateTerrain(
        width: Float,
        height: Float,
        landingPadSize: LandingPadSize,
        numLandingPads: Int = 3,
        seed: Long = 0
    ): Terrain {
        val random = Random(seed)
        
        // Number of terrain points to generate
        val numPoints = 100
        
        // Generate landing pad positions first
        val landingPadPositions = generateLandingPadPositions(numLandingPads, width, random)
        
        // Calculate actual landing pad width based on size
        val baseLandingPadWidth = width / 20 // 5% of screen width
        val actualLandingPadWidth = baseLandingPadWidth * landingPadSize.value
        
        // Generate terrain points
        val points = mutableListOf<Vector2D>()
        val landingPads = mutableListOf<LandingPad>()
        
        // Start with left edge
        points.add(Vector2D(0f, generateTerrainHeight(height, random)))
        
        // Generate points across the width
        val step = width / (numPoints - 1)
        for (i in 1 until numPoints) {
            val x = i * step
            
            // Check if this point is part of a landing pad
            val isLandingPad = landingPadPositions.any { padX ->
                x >= padX - actualLandingPadWidth / 2 && x <= padX + actualLandingPadWidth / 2
            }
            
            if (isLandingPad) {
                // Find which landing pad this is
                val padIndex = landingPadPositions.indexOfFirst { padX ->
                    x >= padX - actualLandingPadWidth / 2 && x <= padX + actualLandingPadWidth / 2
                }
                
                // If this is the start of a landing pad
                if (x <= landingPadPositions[padIndex] - actualLandingPadWidth / 2 + step) {
                    val padStartX = landingPadPositions[padIndex] - actualLandingPadWidth / 2
                    val padEndX = landingPadPositions[padIndex] + actualLandingPadWidth / 2
                    val padY = height * 0.8f // Landing pads at 80% of screen height
                    
                    // Add landing pad start point
                    points.add(Vector2D(padStartX, padY))
                    
                    // Add landing pad end point
                    points.add(Vector2D(padEndX, padY))
                    
                    // Register the landing pad
                    landingPads.add(LandingPad(
                        start = Vector2D(padStartX, padY),
                        end = Vector2D(padEndX, padY)
                    ))
                    
                    // Skip to the end of the landing pad
                    continue
                } else {
                    // Skip points inside the landing pad
                    continue
                }
            }
            
            // Regular terrain point
            points.add(Vector2D(x, generateTerrainHeight(height, random)))
        }
        
        // Create a terrain with a proper getGroundHeight implementation
        return InterpolatedTerrain(points, landingPads)
    }
    
    /**
     * Generates random positions for landing pads.
     */
    private fun generateLandingPadPositions(numPads: Int, width: Float, random: Random): List<Float> {
        val positions = mutableListOf<Float>()
        val segmentWidth = width / numPads
        
        for (i in 0 until numPads) {
            // Place landing pad in the middle of its segment, with some randomness
            val padX = i * segmentWidth + segmentWidth / 2 + random.nextFloat() * segmentWidth * 0.4f - segmentWidth * 0.2f
            positions.add(padX)
        }
        
        return positions
    }
    
    /**
     * Generates a random height for a terrain point.
     */
    private fun generateTerrainHeight(screenHeight: Float, random: Random): Float {
        // Terrain height varies between 70% and 90% of screen height
        return screenHeight * (0.7f + random.nextFloat() * 0.2f)
    }
    
    /**
     * A terrain implementation that interpolates between points for height calculation.
     */
    private class InterpolatedTerrain(
        points: List<Vector2D>,
        landingPads: List<LandingPad>
    ) : Terrain(points, landingPads) {
        
        override fun getGroundHeight(x: Float): Float {
            // Check if x is before the first point
            if (x <= points.firstOrNull()?.x ?: 0f) {
                return points.firstOrNull()?.y ?: 0f
            }
            
            // Check if x is after the last point
            if (x >= points.lastOrNull()?.x ?: 0f) {
                return points.lastOrNull()?.y ?: 0f
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
            return points.lastOrNull()?.y ?: 0f
        }
    }
}