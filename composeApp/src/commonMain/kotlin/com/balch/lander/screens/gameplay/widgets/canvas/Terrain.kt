package com.balch.lander.screens.gameplay.widgets.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.balch.lander.GameConfig
import com.balch.lander.core.game.TerrainGeneratorImpl
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.utils.impl.KotlinxDateTimeProvider
import com.balch.lander.screens.gameplay.widgets.utils.toDp
import org.jetbrains.compose.ui.tooling.preview.Preview

fun DrawScope.drawTerrain(terrain: Terrain, config: GameConfig) {
    val screenWidth = config.screenWidth
    val screenHeight = config.screenHeight

    val terrainPoints = terrain.points
    if (terrainPoints.size < 2) {
        // Need at least two points to draw a path or line
        return
    }

    val path = Path()
    // Scale the first point and move the path to it
    val firstPoint = terrainPoints[0]
    val startX = firstPoint.x / screenWidth * size.width
    val startY = firstPoint.y / screenHeight * size.height
    path.moveTo(startX, startY)

    // Iterate through the rest of the points and add lines to the path
    for (i in 1 until terrainPoints.size) {
        val point = terrainPoints[i]
        val x = point.x / screenWidth * size.width
        val y = point.y / screenHeight * size.height
        path.lineTo(x, y)
    }

    // Draw the complete path once
    drawPath(
        path = path,
        color = Color.Gray,
        style = Stroke(width = 2f) // Use Stroke style for path outline
    )
}

@Preview
@Composable
fun TerrainPreview() {
    val config = GameConfig()
    val terrain = TerrainGeneratorImpl(KotlinxDateTimeProvider())
        .generateTerrain(config.screenWidth, config.screenHeight)

    val (width, height) = toDp(Vector2D(config.screenWidth, config.screenHeight), config)

    MaterialTheme(colors = darkColors()) {
        Canvas(
            modifier = Modifier
                .width(width)
                .height(height)
        ){
            drawTerrain(terrain, config)
            drawLandingPads(terrain)
        }
    }
}
