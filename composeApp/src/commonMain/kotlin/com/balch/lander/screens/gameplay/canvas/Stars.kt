package com.balch.lander.screens.gameplay.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.balch.lander.GameConfig
import org.jetbrains.compose.ui.tooling.preview.Preview

fun DrawScope.drawStars(config: GameConfig) {
    // Draw stars (simple representation)
    repeat(config.backgoundStarCount) {
        val x = (0..size.width.toInt()).random().toFloat()
        val y = (0..size.height.toInt()).random().toFloat()
        drawCircle(
            color = Color.White,
            radius = (1..3).random().toFloat(),
            center = Offset(x, y)
        )
    }
}

@Preview
@Composable
fun StarsPreview() {
    val config = GameConfig()

    MaterialTheme(colors = darkColors()) {
        Canvas(modifier = Modifier
            .width(600.dp)
            .height(350.dp)
        ) {
            drawStars(config)
        }
    }
}
