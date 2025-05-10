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
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BoxScope.DebugOverlay(
    landerPosition: Vector2D,
    camera: Camera = Camera(),
    fps: Int = 60,
    fontScaler: FontScaler = FontScaler(1f),
    initialExpand: Boolean = false,
    stringFormatter: StringFormatter = StringFormatter()

) {
    var expanded by remember { mutableStateOf(initialExpand) }

    val animatedHeight by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "debugOverlayHeight"
    )

    Column(
        modifier = Modifier
            .fillMaxHeight(.70f)
            .align(Alignment.BottomEnd)
            .padding(bottom = 32.dp, end = 44.dp)
            .safeDrawingPadding()
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
                fontSize = fontScaler.scale(14.sp),
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
                fontSize = fontScaler.scale(14.sp),
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                }
            )
        }

        // Content - only visible when expanded
        if (animatedHeight > 0) {
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = animatedHeight
                        scaleY = animatedHeight
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    },
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Lander: (${landerPosition.x.toInt()}, ${landerPosition.y.toInt()})",
                    color = MaterialTheme.colors.onBackground,
                    fontSize = fontScaler.scale(12.sp)
                )
                Text(
                    text = "Camera Offset: (${camera.offset.x.toInt()}, ${camera.offset.y.toInt()})",
                    color = MaterialTheme.colors.onBackground,
                    fontSize = fontScaler.scale(12.sp)
                )
                Text(
                    text = "Camera Scale: ${stringFormatter.formatToString(camera.zoomLevel.scale)}",
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
    val camera = Camera(
        zoomLevel = CameraZoomLevel.MEDIUM,
        offset = Vector2D(1.25f, 10111111f)
    )

    MaterialTheme(colors = darkColors()) {
        Box(modifier = Modifier
            .width(300.dp)
            .height(300.dp)
            .background(Color.Black)
        ) {
            DebugOverlay(
                landerPosition = position,
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
    val camera = Camera()

    MaterialTheme(colors = darkColors()) {
        Box(modifier = Modifier
            .width(300.dp)
            .height(250.dp)
            .background(Color.Black)
        ) {
            DebugOverlay(
                landerPosition = position,
                camera = camera,
                fps = 60
            )
        }
    }
}
