package com.balch.lander.screens.gameplay.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.balch.lander.GameConfig
import com.balch.lander.core.game.TerrainGeneratorImpl
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.utils.impl.KotlinxDateTimeProvider
import com.balch.lander.screens.gameplay.FlightStatus
import com.balch.lander.screens.gameplay.LanderState
import com.balch.lander.screens.gameplay.widgets.canvas.drawLander
import com.balch.lander.screens.gameplay.widgets.canvas.drawLandingPads
import com.balch.lander.screens.gameplay.widgets.canvas.drawStars
import com.balch.lander.screens.gameplay.widgets.canvas.drawTerrain
import com.balch.lander.screens.gameplay.widgets.utils.toDp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GameCanvas(
    landerState: LanderState,
    terrain: Terrain,
    config: GameConfig,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawStars(config)
        drawTerrain(terrain, config)
        drawLandingPads(terrain)
        drawLander(landerState, config)
    }
}

@Preview
@Composable
fun GameCanvasPreview() {
    val landerState = LanderState(
        position = Vector2D(500f, 100f),
        thrustStrength = ThrustStrength.HIGH,
        rotation = 30f,
        flightStatus = FlightStatus.WARNING
    )
    val config = GameConfig()
    val terrain = TerrainGeneratorImpl(KotlinxDateTimeProvider())
        .generateTerrain(config.screenWidth, config.screenHeight)

    val (width, height) = toDp(Vector2D(config.screenWidth, config.screenHeight), config)

    MaterialTheme(colors = darkColors()) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .background(Color.Black)
        ) {
            GameCanvas(
                landerState = landerState,
                terrain = terrain,
                config = config,
            )
        }
    }
}