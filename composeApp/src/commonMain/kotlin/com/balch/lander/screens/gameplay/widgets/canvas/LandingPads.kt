package com.balch.lander.screens.gameplay.widgets.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.balch.lander.core.game.models.Terrain

fun DrawScope.drawLandingPads(terrain: Terrain) {
    terrain.landingPads.forEach { pad ->
        val x1 = pad.start.x / 1000f * size.width
        val y1 = pad.start.y / 1000f * size.height
        val x2 = pad.end.x / 1000f * size.width
        val y2 = pad.end.y / 1000f * size.height

        // Draw landing pad
        drawLine(
            color = Color.Green,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 3f
        )
    }
}
