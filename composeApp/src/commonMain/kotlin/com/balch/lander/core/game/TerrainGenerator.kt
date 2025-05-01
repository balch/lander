package com.balch.lander.core.game

import com.balch.lander.LandingPadSize
import com.balch.lander.core.game.models.LandingPad
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.utils.TimeUtil
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.random.Random

/**
 * Generates random terrain for the Lunar Lander game.
 */
class TerrainGenerator {
    /**
     * Data class to hold information about a landing pad.
     */
    private data class LandingPadInfo(
        val position: Float,
        val isInCrater: Boolean
    )

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
        seed: Long = TimeUtil.currentTimeMillis()
    ): Terrain {
        val random = Random(seed)

        // Calculate actual landing pad width based on size
        val baseLandingPadWidth = width / 10
        val actualLandingPadWidth = baseLandingPadWidth * landingPadSize.value

        // Number of terrain points to generate
        val numPoints = 100

        // Generate landing pad positions first
        val landingPadInfos = generateLandingPadPositions(
            numPads = numLandingPads,
            actualLandingPadWidth = actualLandingPadWidth,
            width = width,
            random = random
        )

        // Generate terrain points
        val points = mutableListOf<Vector2D>()
        val landingPads = mutableListOf<LandingPad>()

        // Create some rock formations for visual interest
        val numRocks = random.nextInt(3, 7) // 3-6 rock formations
        val rockPositions = (0 until numRocks).map {
            width * random.nextFloat() // Random position across width
        }

        // Create some mountain features
        val numMountains = random.nextInt(2, 5) // 2-4 mountains
        val mountainPositions = (0 until numMountains).map {
            width * random.nextFloat() // Random position across width
        }

        // Start with left edge
        points.add(Vector2D(0f, generateTerrainHeight(height, random)))

        // Generate points across the width
        val step = width / (numPoints - 1)
        for (i in 1 until numPoints) {
            val x = i * step

            // Check if this point is part of a landing pad
            val landingPadInfo = landingPadInfos.find { padInfo ->
                x >= padInfo.position - actualLandingPadWidth / 2 && x <= padInfo.position + actualLandingPadWidth / 2
            }

            if (landingPadInfo != null) {
                // If this is the start of a landing pad
                if (x <= landingPadInfo.position - actualLandingPadWidth / 2 + step) {
                    val padStartX = landingPadInfo.position - actualLandingPadWidth / 2
                    val padEndX = landingPadInfo.position + actualLandingPadWidth / 2

                    // Determine landing pad height based on whether it's in a crater
                    val baseHeight = height * 0.8f // Default landing pad height
                    val padY = if (landingPadInfo.isInCrater) {
                        // Place the landing pad deeper if it's in a crater
                        baseHeight + height * 0.05f // 5% deeper
                    } else {
                        baseHeight
                    }

                    // Add landing pad start point
                    points.add(Vector2D(padStartX, padY))

                    // Add landing pad end point
                    points.add(Vector2D(padEndX, padY))

                    // Register the landing pad
                    landingPads.add(
                        LandingPad(
                            start = Vector2D(padStartX, padY),
                            end = Vector2D(padEndX, padY)
                        )
                    )

                    // Skip to the end of the landing pad
                    continue
                } else {
                    // Skip points inside the landing pad
                    continue
                }
            }

            // Generate base terrain height
            var terrainHeight = generateTerrainHeight(height, random)

            // Apply crater effects around landing pads
            for (padInfo in landingPadInfos) {
                if (padInfo.isInCrater) {
                    // Create a larger crater around landing pads that are in craters
                    val craterWidth = actualLandingPadWidth * 3f // Crater is 3x wider than the landing pad
                    val craterDepth = height * 0.05f // 5% of screen height
                    terrainHeight = createCrater(x, padInfo.position, terrainHeight, craterWidth, craterDepth, random)
                } else {
                    // Create smaller crater-like depressions around other landing pads
                    val craterWidth = actualLandingPadWidth * 1.5f // Smaller crater
                    val craterDepth = height * 0.02f // 2% of screen height
                    terrainHeight = createCrater(x, padInfo.position, terrainHeight, craterWidth, craterDepth, random)
                }
            }

            // Apply mountain features (larger and taller than rocks)
            for (mountainPos in mountainPositions) {
                val mountainWidth = width * 0.1f // 10% of screen width
                val mountainHeight = height * 0.15f * (0.7f + random.nextFloat() * 0.3f) // 10.5-15% of screen height
                terrainHeight = createMountain(x, mountainPos, terrainHeight, mountainWidth, mountainHeight, random)
            }

            // Apply rock formations (smaller than mountains)
            for (rockPos in rockPositions) {
                val rockWidth = width * 0.03f // 3% of screen width
                val rockHeight = height * 0.04f * random.nextFloat() // Up to 4% of screen height
                terrainHeight = createRock(x, rockPos, terrainHeight, rockWidth, rockHeight, random)
            }

            // Add the terrain point
            points.add(Vector2D(x, terrainHeight))
        }

        // Apply smoothing pass to reduce jagged edges
        val smoothedPoints = smoothTerrainPoints(points, landingPads)

        // Create a terrain with a proper getGroundHeight implementation
        return Terrain(smoothedPoints, landingPads)
    }

    /**
     * Generates random positions for landing pads.
     * Some landing pads may be placed in craters.
     */
    private fun generateLandingPadPositions(
        numPads: Int,
        actualLandingPadWidth: Float,
        width: Float,
        random: Random
    ): List<LandingPadInfo> {
        val positions = mutableListOf<LandingPadInfo>()
        val segmentWidth = width / numPads

        for (i in 0 until numPads) {
            // Place landing pad in the middle of its segment, with some randomness
            val padX = generateRandomLandingPadPosition(
                index = i,
                segmentWidth = segmentWidth,
                actualLandingPadWidth = actualLandingPadWidth,
                random = random
            )

            // Randomly decide if this landing pad should be in a crater
            val isInCrater = random.nextFloat() < 0.4f // 40% chance to be in a crater

            positions.add(LandingPadInfo(padX, isInCrater))
        }

        return positions
    }

    private fun generateRandomLandingPadPosition(
        index: Int,
        segmentWidth: Float,
        actualLandingPadWidth: Float,
        random: Random
    ): Float {
        fun randomPos() =
            index * segmentWidth + segmentWidth / 2 + random.nextFloat() * segmentWidth * 0.4f - segmentWidth * 0.2f

        // make sure landing pad is not in the center of the screen, as it makes it too easy to land
        var posX = randomPos()
        while (posX in 500-actualLandingPadWidth/2..500+actualLandingPadWidth/2)
            posX = randomPos()

        return posX
    }

    /**
     * Creates a crater-like depression in the terrain.
     * 
     * @param x Current x-coordinate
     * @param xPos Center x-coordinate of the crater
     * @param baseHeight Base height of the terrain
     * @param width Width of the crater
     * @param depth Depth of the crater
     * @param random Random generator for variations
     * @return Height at the given x-coordinate considering the crater
     */
    private fun createCrater(x: Float, xPos: Float, baseHeight: Float, width: Float, depth: Float, random: Random): Float {
        val distance = abs(x - xPos)
        if (distance > width) return baseHeight

        // Calculate crater shape using a cosine function
        val normalizedDist = (distance / width) * PI.toFloat()
        val craterFactor = (cos(normalizedDist) + 1) / 2 // 0 to 1, highest at center

        // Add some randomness to the crater shape
        val randomFactor = 1f + (random.nextFloat() * 0.2f - 0.1f) // ±10% variation

        // Calculate crater depth, deeper at center
        val actualDepth = depth * craterFactor * randomFactor

        return baseHeight + actualDepth
    }

    /**
     * Creates a rock formation in the terrain with smoother edges.
     * 
     * @param x Current x-coordinate
     * @param xPos Center x-coordinate of the rock
     * @param baseHeight Base height of the terrain
     * @param width Width of the rock
     * @param height Height of the rock
     * @param random Random generator for variations
     * @return Height at the given x-coordinate considering the rock
     */
    private fun createRock(x: Float, xPos: Float, baseHeight: Float, width: Float, height: Float, random: Random): Float {
        val distance = abs(x - xPos)
        if (distance > width) return baseHeight

        // Calculate rock shape using a cosine function for a smoother appearance
        val normalizedDist = (distance / width) * PI.toFloat()
        // Use cosine squared for smoother transitions at the edges
        val rockFactor = cos(normalizedDist).pow(2) // 0 to 1, smoother transition

        // Add some randomness to the rock shape, but less than before
        val randomFactor = 1f + (random.nextFloat() * 0.2f - 0.1f) // ±10% variation

        // Calculate rock height, varies across width
        val actualHeight = height * rockFactor * randomFactor

        return baseHeight - actualHeight // Subtract because higher y is lower on screen
    }

    /**
     * Creates a mountain-like feature in the terrain.
     * 
     * @param x Current x-coordinate
     * @param xPos Center x-coordinate of the mountain
     * @param baseHeight Base height of the terrain
     * @param width Width of the mountain
     * @param height Height of the mountain
     * @param random Random generator for variations
     * @return Height at the given x-coordinate considering the mountain
     */
    private fun createMountain(x: Float, xPos: Float, baseHeight: Float, width: Float, height: Float, random: Random): Float {
        val distance = abs(x - xPos)
        if (distance > width) return baseHeight

        // Calculate mountain shape using a smooth bell curve
        val normalizedDist = (distance / width)
        // Use a bell curve for a natural mountain shape
        val mountainFactor = (1f - normalizedDist.pow(2)).pow(2)

        // Add some randomness to the mountain shape
        val randomFactor = 1f + (random.nextFloat() * 0.15f - 0.075f) // ±7.5% variation

        // Calculate mountain height
        val actualHeight = height * mountainFactor * randomFactor

        return baseHeight - actualHeight // Subtract because higher y is lower on screen
    }

    /**
     * Generates a random height for a terrain point.
     * Uses a smoother algorithm to avoid jagged edges.
     */
    private fun generateTerrainHeight(screenHeight: Float, random: Random): Float {
        // Base height varies between 70% and 90% of screen height, but with smoother distribution
        val baseHeight = screenHeight * (0.7f + (random.nextFloat().pow(2) + random.nextFloat().pow(2)) / 10f)

        // Add some small-scale smoothing
        val smoothingFactor = 0.05f * screenHeight * (random.nextFloat() - 0.5f)

        return baseHeight + smoothingFactor
    }

    /**
     * Applies multiple smoothing passes to the terrain points to reduce jagged edges.
     * Preserves landing pads by not smoothing their points.
     * 
     * @param points Original terrain points
     * @param landingPads Landing pads to preserve
     * @param passes Number of smoothing passes to apply
     * @return Smoothed terrain points
     */
    private fun smoothTerrainPoints(
        points: List<Vector2D>,
        landingPads: List<LandingPad>,
        passes: Int = 3
    ): List<Vector2D> {
        // If there are too few points, no need to smooth
        if (points.size <= 3) return points

        // Create a set of x-coordinates for landing pad points to preserve them
        val landingPadXCoords = mutableSetOf<Float>()
        for (pad in landingPads) {
            landingPadXCoords.add(pad.start.x)
            landingPadXCoords.add(pad.end.x)
        }

        // Apply multiple passes of smoothing
        var currentPoints = points
        repeat(passes) {
            val result = mutableListOf<Vector2D>()

            // Add first point unchanged
            result.add(currentPoints.first())

            // Apply smoothing to interior points
            for (i in 1 until currentPoints.size - 1) {
                val current = currentPoints[i]

                // Skip smoothing for landing pad points
                if (current.x in landingPadXCoords) {
                    result.add(current)
                    continue
                }

                // Get neighboring points
                val prev = currentPoints[i - 1]
                val next = currentPoints[i + 1]

                // Apply weighted moving average (current point has more weight)
                val smoothedY = (prev.y + current.y * 2 + next.y) / 4f

                result.add(Vector2D(current.x, smoothedY))
            }

            // Add last point unchanged
            result.add(currentPoints.last())

            // Update current points for next pass
            currentPoints = result
        }

        return currentPoints
    }
}
