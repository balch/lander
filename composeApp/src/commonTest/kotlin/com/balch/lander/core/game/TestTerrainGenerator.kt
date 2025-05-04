package com.balch.lander.core.game

import com.balch.lander.LandingPadSize
import com.balch.lander.core.game.models.Terrain

class TestTerrainGenerator(
    private val terrain: Terrain,
): TerrainGenerator {

    override fun generateTerrain(
        width: Float,
        height: Float,
        landingPadSize: LandingPadSize,
        numPoints: Int,
    ): Terrain = terrain
}
