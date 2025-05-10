package com.balch.lander.screens.gameplay.widgets.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.balch.lander.GameConfig
import com.balch.lander.core.game.models.Vector2D

@Composable
fun toDp(point: Vector2D, config: GameConfig): Pair<Dp, Dp> {
    // Get the local density for dp conversions
    val density = LocalDensity.current
    val screenWidth = config.screenWidth
    val screenHeight = config.screenHeight

    // Convert game coordinates to dp for offset
    // The game coordinates are in the range 0-1000, so we need to convert to dp
    val screenWidthPx = with(density) { screenWidth.toDp() }
    val screenHeightPx = with(density) { screenHeight.toDp() }

    return Pair(
        with(density) { (point.x / screenWidth * screenWidthPx.toPx()).toDp() },
        with(density) { (point.y / screenHeight * screenHeightPx.toPx()).toDp() }
    )
}
