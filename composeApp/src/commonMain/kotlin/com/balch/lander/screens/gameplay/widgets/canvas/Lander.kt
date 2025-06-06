package com.balch.lander.screens.gameplay.widgets.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.balch.lander.GameConfig
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.screens.gameplay.FlightStatus
import com.balch.lander.screens.gameplay.LanderState
import org.jetbrains.compose.ui.tooling.preview.Preview

fun DrawScope.drawLander(
    landerState: LanderState,
    config: GameConfig,
) {
    // Draw lander
    val landerX = landerState.position.x / config.screenWidth * size.width
    val landerY = landerState.position.y / config.screenHeight * size.height

    val isThrusting = landerState.thrustStrength.value > 0f

    // Determine lander color based on game state
    val landerColor = landerState.flightStatus.color

    val offset = config.landerSize / 2

    // Draw lander
    rotate(landerState.rotation, Offset(landerX, landerY)) {
        // Lander body
        drawRect(
            color = landerColor,
            topLeft = Offset(landerX - offset, landerY - offset),
            size = Size(config.landerSize, config.landerSize)
        )

        // Lander legs
        drawLine(
            color = landerColor,
            start = Offset(landerX - offset, landerY + offset),
            end = Offset(landerX - config.landerSize, landerY + config.landerSize),
            strokeWidth = 2f
        )

        drawLine(
            color = landerColor,
            start = Offset(landerX + offset, landerY + offset),
            end = Offset(landerX + config.landerSize, landerY + config.landerSize),
            strokeWidth = 2f
        )

        // Thrust flame (if thrusting)
        if (isThrusting && landerState.fuel > 0) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(landerX - offset / 2, landerY + offset),
                size = Size(offset, config.landerSize * landerState.thrustStrength.value),
                alpha = 0.5f
            )
        }
    }
}

@Preview
@Composable
fun LanderPreview() {
    val landerState = LanderState(
        thrustStrength = ThrustStrength.MEDIUM,
        flightStatus = FlightStatus.WARNING,
    )
    val config = GameConfig()

    MaterialTheme(colors = darkColors()) {
        Canvas(modifier = Modifier
            .width(25.dp)
            .height(25.dp)
            .offset(12.dp, 12.dp)
        ){
            drawLander(landerState, config)
        }
    }
}
