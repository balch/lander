package com.balch.lander.screens.gameplay.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.utils.FontScaler
import com.balch.lander.screens.gameplay.GameStatus
import com.balch.lander.screens.gameplay.LanderState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BoxScope.DrawControlPanel(
    landerState: LanderState,
    onControlInputs: (ControlInputs) -> Unit,
    fontScaler: FontScaler = FontScaler(1f),
) {
    if (landerState.status == GameStatus.PLAYING) {

        // Control panel in the top right area
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 44.dp)
                .safeDrawingPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thrust control buttons (top row)

            // Rotation control interaction sources
            val rotateLeftInteractionSource = remember { MutableInteractionSource() }
            val isRotateLeftPressed by rotateLeftInteractionSource.collectIsPressedAsState()

            val rotateRightInteractionSource = remember { MutableInteractionSource() }
            val isRotateRightPressed by rotateRightInteractionSource.collectIsPressedAsState()

            // Track selected thrust level
            val lowThrustInteractionSource = remember { MutableInteractionSource() }
            val isLowThrustPressed by lowThrustInteractionSource.collectIsPressedAsState()

            val midThrustInteractionSource = remember { MutableInteractionSource() }
            val isMidThrustPressed by midThrustInteractionSource.collectIsPressedAsState()

            val hiThrustInteractionSource = remember { MutableInteractionSource() }
            val isHiThrustPressed by hiThrustInteractionSource.collectIsPressedAsState()

            // Top row - Thrust controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Low button
                Button(
                    onClick = { /* Handled by interaction source */ },
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF004000)
                    ),
                    contentPadding = PaddingValues(4.dp),
                    border = BorderStroke(2.dp, Color(0xFFAA5500)),
                    interactionSource = lowThrustInteractionSource
                ) {
                    Text(
                        text = ThrustStrength.LOW.label,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = fontScaler.scale(14.sp),
                    )
                }

                // Mid button
                Button(
                    onClick = { /* Handled by interaction source */ },
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF000080)
                    ),
                    contentPadding = PaddingValues(4.dp),
                    border = BorderStroke(2.dp, Color(0xFFAA5500)),
                    interactionSource = midThrustInteractionSource
                ) {
                    Text(
                        text = ThrustStrength.MEDIUM.label,
                        fontSize = fontScaler.scale(14.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                // Hi button
                Button(
                    onClick = { /* Handled by interaction source */ },
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFA52A2A)
                    ),
                    contentPadding = PaddingValues(4.dp),
                    border = BorderStroke(2.dp, Color(0xFFAA5500)),
                    interactionSource = hiThrustInteractionSource
                ) {
                    Text(
                        text = ThrustStrength.HIGH.label,
                        fontSize = fontScaler.scale(14.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom row - Rotation controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rotate left button
                Button(
                    onClick = { /* Handled by interaction source */ },
                    modifier = Modifier.size(width = 80.dp, height = 40.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4B0082)),
                    contentPadding = PaddingValues(4.dp),
                    border = BorderStroke(2.dp, Color(0xFFAA5500)),
                    interactionSource = rotateLeftInteractionSource
                ) {
                    Text(
                        text = "<--",
                        fontSize = fontScaler.scale(14.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                // Rotate right button
                Button(
                    onClick = { /* Handled by interaction source */ },
                    modifier = Modifier.size(width = 80.dp, height = 40.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4B0082)),
                    contentPadding = PaddingValues(4.dp),
                    border = BorderStroke(2.dp, Color(0xFFAA5500)),
                    interactionSource = rotateRightInteractionSource,
                ) {
                    Text(
                        text = "-->",
                        fontSize = fontScaler.scale(14.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            val lastControlInputs = remember { mutableStateOf<ControlInputs?>(null) }
            val controlInputs = ControlInputs(
                thrustStrength = when {
                    isLowThrustPressed -> ThrustStrength.LOW
                    isMidThrustPressed -> ThrustStrength.MEDIUM
                    isHiThrustPressed -> ThrustStrength.HIGH
                    else -> ThrustStrength.OFF
                },
                rotateRight = isRotateRightPressed,
                rotateLeft = isRotateLeftPressed,
            )
            if (lastControlInputs.value != controlInputs) {
                lastControlInputs.value = controlInputs
                onControlInputs(controlInputs)
            }
        }
    }
}

@Preview
@Composable
fun ControlPanelPreview() {
    val landerState = LanderState()

    MaterialTheme(colors = darkColors()) {
        Box(modifier = Modifier
            .width(600.dp)
            .height(350.dp)
        ) {
            DrawControlPanel(landerState, {  })
        }
    }
}
