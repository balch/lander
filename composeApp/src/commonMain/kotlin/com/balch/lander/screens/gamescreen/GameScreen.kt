package com.balch.lander.screens.gamescreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Game Screen for the Lunar Lander game.
 * Displays the game area, lander information, and controls.
 */
@Composable
fun GameScreen(
    uiState: GameScreenState,
    onThrustPressed: () -> Unit,
    onThrustReleased: () -> Unit,
    onRotateLeftPressed: () -> Unit,
    onRotateLeftReleased: () -> Unit,
    onRotateRightPressed: () -> Unit,
    onRotateRightReleased: () -> Unit,
    onRestartClicked: () -> Unit,
    onBackToStartClicked: () -> Unit
) {
    val gameState = uiState.gameState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Game area (would contain the actual game rendering)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw stars (simple representation)
            repeat(100) {
                val x = (0..size.width.toInt()).random().toFloat()
                val y = (0..size.height.toInt()).random().toFloat()
                drawCircle(
                    color = Color.White,
                    radius = (1..2).random().toFloat(),
                    center = Offset(x, y)
                )
            }

            // Draw terrain
            val terrainPath = gameState.terrain.points
            if (terrainPath.isNotEmpty()) {
                for (i in 0 until terrainPath.size - 1) {
                    val p1 = terrainPath[i]
                    val p2 = terrainPath[i + 1]

                    // Scale points to canvas size
                    val x1 = p1.x / 1000f * size.width
                    val y1 = p1.y / 1000f * size.height
                    val x2 = p2.x / 1000f * size.width
                    val y2 = p2.y / 1000f * size.height

                    // Draw terrain line
                    drawLine(
                        color = Color.Gray,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2f
                    )
                }
            }

            // Draw landing pads
            gameState.terrain.landingPads.forEach { pad ->
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

            // Draw lander
            val landerX = gameState.position.x / 1000f * size.width
            val landerY = gameState.position.y / 1000f * size.height
            val landerSize = 20f

            // Determine lander color based on game state
            val landerColor = when {
                gameState.isDangerMode -> Color.Red
                gameState.isThrusting -> Color.Yellow
                else -> Color.White
            }

            // Draw lander
            rotate(gameState.rotation, Offset(landerX, landerY)) {
                // Lander body
                drawRect(
                    color = landerColor,
                    topLeft = Offset(landerX - landerSize / 2, landerY - landerSize / 2),
                    size = Size(landerSize, landerSize)
                )

                // Lander legs
                drawLine(
                    color = landerColor,
                    start = Offset(landerX - landerSize / 2, landerY + landerSize / 2),
                    end = Offset(landerX - landerSize, landerY + landerSize),
                    strokeWidth = 2f
                )

                drawLine(
                    color = landerColor,
                    start = Offset(landerX + landerSize / 2, landerY + landerSize / 2),
                    end = Offset(landerX + landerSize, landerY + landerSize),
                    strokeWidth = 2f
                )

                // Thrust flame (if thrusting)
                if (gameState.isThrusting && gameState.fuel > 0) {
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(landerX - landerSize / 4, landerY + landerSize / 2),
                        size = Size(landerSize / 2, landerSize / 2)
                    )
                }
            }
        }

        // Lander information panel
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color(0x80000000))
                .padding(8.dp)
        ) {
            Text(
                text = "FUEL: ${gameState.fuel.toInt()}/${gameState.initialFuel.toInt()}",
                color = if (gameState.fuel < 20) Color.Red else MaterialTheme.colors.onBackground
            )

            Text(
                text = "DESCENT: ${abs(gameState.velocity.y).toInt()} m/s",
                color = if (abs(gameState.velocity.y) > 3) Color.Red else MaterialTheme.colors.onBackground
            )

            Text(
                text = "DRIFT: ${gameState.velocity.x.toInt()} m/s",
                color = if (abs(gameState.velocity.x) > 2) Color.Red else MaterialTheme.colors.onBackground
            )

            Text(
                text = "ALTITUDE: ${gameState.distanceToGround.toInt()} m",
                color = if (gameState.distanceToGround < 50) Color.Red else MaterialTheme.colors.onBackground
            )
        }

        // Small button on the bottom right of the screen
        if (gameState.status == GameStatus.PLAYING) {
            // Small button in the bottom right corner
            Button(
                onClick = { /* Add functionality here */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(40.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSecondary
                )
            }

            // Main game controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Rotate left button
                val rotateLeftInteractionSource = remember { MutableInteractionSource() }
                val isRotateLeftPressed by rotateLeftInteractionSource.collectIsPressedAsState()

                if (isRotateLeftPressed) {
                    onRotateLeftPressed()
                } else {
                    onRotateLeftReleased()
                }

                Button(
                    onClick = { /* Handled by interaction source */ },
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                    contentPadding = PaddingValues(0.dp),
                    interactionSource = rotateLeftInteractionSource
                ) {
                    Text(
                        text = "←",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onPrimary
                    )
                }

                // Thrust button
                val thrustInteractionSource = remember { MutableInteractionSource() }
                val isThrustPressed by thrustInteractionSource.collectIsPressedAsState()

                if (isThrustPressed) {
                    onThrustPressed()
                } else {
                    onThrustReleased()
                }

                Button(
                    onClick = { /* Handled by interaction source */ },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (gameState.isThrusting) 
                            MaterialTheme.colors.secondary 
                        else 
                            MaterialTheme.colors.primary
                    ),
                    contentPadding = PaddingValues(0.dp),
                    interactionSource = thrustInteractionSource
                ) {
                    Text(
                        text = "▲",
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.onPrimary
                    )
                }

                // Rotate right button
                val rotateRightInteractionSource = remember { MutableInteractionSource() }
                val isRotateRightPressed by rotateRightInteractionSource.collectIsPressedAsState()

                if (isRotateRightPressed) {
                    onRotateRightPressed()
                } else {
                    onRotateRightReleased()
                }

                Button(
                    onClick = { /* Handled by interaction source */ },
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                    contentPadding = PaddingValues(0.dp),
                    interactionSource = rotateRightInteractionSource
                ) {
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }

        // Game over overlay
        if (gameState.status != GameStatus.PLAYING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(16.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Game result
                        Text(
                            text = if (gameState.status == GameStatus.LANDED) "MISSION SUCCESSFUL" else "MISSION FAILED",
                            style = MaterialTheme.typography.h5,
                            color = if (gameState.status == GameStatus.LANDED) 
                                Color.Green 
                            else 
                                Color.Red,
                            textAlign = TextAlign.Center
                        )

                        // Result message
                        Text(
                            text = uiState.selectedMessage,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface,
                            textAlign = TextAlign.Center
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = onRestartClicked,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                )
                            ) {
                                Text("TRY AGAIN")
                            }

                            Button(
                                onClick = onBackToStartClicked,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.secondary
                                )
                            ) {
                                Text("MAIN MENU")
                            }
                        }
                    }
                }
            }
        }
    }
}
