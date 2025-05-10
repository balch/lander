package com.balch.lander.screens.gameplay.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balch.lander.CameraZoomLevel
import com.balch.lander.core.game.Camera
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.utils.FontScaler
import com.balch.lander.core.utils.StringFormatter
import com.balch.lander.screens.gameplay.LanderState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ColumnScope.DebugOverlay(
    landerState: LanderState,
    camera: Camera = Camera(),
    fps: Int = 60,
    fontScaler: FontScaler = FontScaler(1f),
    initialExpand: Boolean = false,
    stringFormatter: StringFormatter = StringFormatter(),
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(initialExpand) }

    val animatedHeight by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "debugOverlayHeight"
    )

    Column(
        modifier = modifier
            .align(Alignment.End)
            .background(Color(0x33000000), shape = RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.End,
    ) {
        // Header - always visible
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Debug Info",
                color = MaterialTheme.colors.onBackground,
                fontSize = fontScaler.scale(12.sp),
                fontWeight = FontWeight.Bold
            )

            // Rotation animation for the arrow
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "arrowRotation"
            )

            Text(
                text = "▼", // Down arrow that will rotate
                color = MaterialTheme.colors.onBackground,
                fontSize = fontScaler.scale(12.sp),
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                }
            )
        }

        // Content - only visible when expanded
        if (animatedHeight > 0) {
            val position = landerState.position

            Column(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = animatedHeight
                        scaleY = animatedHeight
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    },
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(
                    text = "Position: (x=${position.x.toInt()}, y=${position.y.toInt()}, rot=${stringFormatter.formatToString(landerState.rotation)})",
                    color = MaterialTheme.colors.onBackground,
                    fontSize = fontScaler.scale(12.sp),
                )
                Text(
                    text = "Height: (sea=${landerState.distanceToSeaLevel.toInt()}, grd=${landerState.distanceToGround.toInt()})",
                    color = MaterialTheme.colors.onBackground,
                    fontSize = fontScaler.scale(12.sp)
                )
                Text(
                    text = "Velocity: (x=${stringFormatter.formatToString(landerState.velocity.x)}, y=${stringFormatter.formatToString(landerState.velocity.y)})",
                    color = MaterialTheme.colors.onBackground,
                    fontSize = fontScaler.scale(12.sp)
                )
                Text(
                    text = "Camera: (x=${camera.offset.x.toInt()}, y=${camera.offset.y.toInt()}, scale=${stringFormatter.formatToString(camera.zoomLevel.scale)})",
                    color = MaterialTheme.colors.onBackground,
                    fontSize = fontScaler.scale(12.sp)
                )
                Text(
                    text = "FPS: $fps",
                    color = MaterialTheme.colors.onBackground,
                    fontSize = fontScaler.scale(12.sp),
                )
            }
        }
    }
}

@Preview
@Composable
fun DebugOverlayExpandedPreview() {
    val position = Vector2D(500f, 100f)
    val landerState = LanderState(
        position = position,
        rotation = 30f,
        velocity = Vector2D(10f, 10f),
        distanceToSeaLevel = 850f,
        distanceToGround = 800f,
    )
    val camera = Camera(
        zoomLevel = CameraZoomLevel.MEDIUM,
        offset = Vector2D(1.25f, 10111111f)
    )

    MaterialTheme(colors = darkColors()) {
        Column(modifier = Modifier
            .width(300.dp)
            .height(300.dp)
            .background(Color.Black)
        ) {
            DebugOverlay(
                landerState = landerState,
                camera = camera,
                fps = 60,
                initialExpand = true,
            )
        }
    }
}

@Preview
@Composable
fun DebugOverlayPreview() {
    val position = Vector2D(500f, 100f)
    val landerState = LanderState(
        position = position,
        rotation = 30f,
        velocity = Vector2D(10f, 10f),
        distanceToSeaLevel = 850f,
        distanceToGround = 800f,
    )
    val camera = Camera(
        zoomLevel = CameraZoomLevel.MEDIUM,
        offset = Vector2D(1.25f, 10111111f)
    )

    MaterialTheme(colors = darkColors()) {
        Column(modifier = Modifier
            .width(300.dp)
            .height(250.dp)
            .background(Color.Black)
        ) {
            DebugOverlay(
                landerState = landerState,
                camera = camera,
                fps = 60
            )
        }
    }
}
