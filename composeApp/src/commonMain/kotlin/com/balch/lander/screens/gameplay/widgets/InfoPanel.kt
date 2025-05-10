package com.balch.lander.screens.gameplay.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balch.lander.core.utils.FontScaler
import com.balch.lander.core.utils.StringFormatter
import com.balch.lander.screens.gameplay.LanderState
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs

@Composable
fun BoxScope.DrawInfoPanel(
    landerState: LanderState,
    fontScaler: FontScaler = FontScaler(1f),
    stringFormatter: StringFormatter = StringFormatter()
) {

    // Lander information panel
    Column(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(24.dp)
            .safeDrawingPadding()
            .background(Color(0x00000000))
    ) {
        Text(
            text = "FUEL: ${landerState.fuel.toInt()}/${landerState.initialFuel.toInt()}",
            color = if (landerState.fuel < 20) Color.Red else MaterialTheme.colors.onBackground,
            fontSize = fontScaler.scale(14.sp),
        )

        Text(
            text = "DESCENT: ${abs(landerState.velocity.y).toInt()} m/s",
            color = if (abs(landerState.velocity.y) > 3) Color.Red else MaterialTheme.colors.onBackground,
            fontSize = fontScaler.scale(14.sp),
        )

        Text(
            text = "DRIFT: ${stringFormatter.formatToString(landerState.velocity.x)} m/s",
            color = if (abs(landerState.velocity.x) > 2) Color.Red else MaterialTheme.colors.onBackground,
            fontSize = fontScaler.scale(14.sp),
        )

        Text(
            text = "ALTITUDE: ${landerState.distanceToGround.toInt()} m",
            color = if (landerState.distanceToGround < 50) Color.Red else MaterialTheme.colors.onBackground,
            fontSize = fontScaler.scale(14.sp),
        )
    }
}

@Preview
@Composable
fun InfoPanelPreview() {
    val landerState = LanderState()

    MaterialTheme(colors = darkColors()) {
        Box(modifier = Modifier
            .width(600.dp)
            .height(350.dp)
        ) {
            DrawInfoPanel(landerState)
        }
    }
}
