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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.balch.lander.GameConfig
import com.balch.lander.screens.gamescreen.gameplay.ControlInputs
import kotlin.math.abs

/**
 * Game Screen for the Lunar Lander game.
 * Displays the game area, lander information, and controls.
 */
@Composable
fun GameScreen(
    uiState: GameScreenState,
    onControlInputs: (ControlInputs) -> Unit,
    onRestartClicked: () -> Unit,
    onBackToStartClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        when (uiState) {
            is GameScreenState.GameOver -> GameOverContent(uiState, onRestartClicked, onBackToStartClicked)
            GameScreenState.Loading -> LoadingContent()
            GameScreenState.NavigateToStartScreen -> TODO()
            is GameScreenState.Playing -> PlayingContent(
                state = uiState,
                onControlInputs = onControlInputs,
            )
        }
    }
}
@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun BoxScope.PlayingContent(
    state: GameScreenState.Playing,
    onControlInputs: (ControlInputs) -> Unit,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawStars(state.environmentState.config)
        drawTerrain(state.environmentState.terrain)
        drawLandingPads(state.environmentState.terrain)
        drawLander(state.landerState)
    }
    drawInfoPanel(state.landerState, state.fps)
    drawControlPanel(state.landerState, onControlInputs)
}

fun DrawScope.drawTerrain(terrain: Terrain) {
    val terrainPoints = terrain.points
    if (terrainPoints.size < 2) {
        // Need at least two points to draw a path or line
        return
    }

    val path = Path()
    // Scale the first point and move the path to it
    val firstPoint = terrainPoints[0]
    val startX = firstPoint.x / 1000f * size.width
    val startY = firstPoint.y / 1000f * size.height
    path.moveTo(startX, startY)

    // Iterate through the rest of the points and add lines to the path
    for (i in 1 until terrainPoints.size) {
        val point = terrainPoints[i]
        val x = point.x / 1000f * size.width
        val y = point.y / 1000f * size.height
        path.lineTo(x, y)
    }

    // Draw the complete path once
    drawPath(
        path = path,
        color = Color.Gray,
        style = Stroke(width = 2f) // Use Stroke style for path outline
    )
}

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

fun DrawScope.drawLander(landerState: LanderState) {
    // Draw lander
    val landerX = landerState.position.x / 1000f * size.width
    val landerY = landerState.position.y / 1000f * size.height
    val landerSize = 20f

    // Determine lander color based on game state
    val landerColor = when {
        landerState.isDangerMode -> Color.Red
        landerState.isThrusting -> Color.Yellow
        else -> Color.White
    }

    // Draw lander
    rotate(landerState.rotation, Offset(landerX, landerY)) {
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
        if (landerState.isThrusting && landerState.fuel > 0) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(landerX - landerSize / 4, landerY + landerSize / 2),
                size = Size(landerSize / 2, landerSize / 2)
            )
        }
    }
}

@Composable
fun BoxScope.drawInfoPanel(landerState: LanderState, fps: Int) {
    // Lander information panel
    Column(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(16.dp)
            .background(Color(0x80000000))
            .padding(8.dp)
    ) {
        Text(
            text = "FUEL: ${landerState.fuel.toInt()}/${landerState.initialFuel.toInt()}",
            color = if (landerState.fuel < 20) Color.Red else MaterialTheme.colors.onBackground
        )

        Text(
            text = "DESCENT: ${abs(landerState.velocity.y).toInt()} m/s",
            color = if (abs(landerState.velocity.y) > 3) Color.Red else MaterialTheme.colors.onBackground
        )

        Text(
            text = "DRIFT: ${landerState.velocity.x.toInt()} m/s",
            color = if (abs(landerState.velocity.x) > 2) Color.Red else MaterialTheme.colors.onBackground
        )

        Text(
            text = "ALTITUDE: ${landerState.distanceToGround.toInt()} m",
            color = if (landerState.distanceToGround < 50) Color.Red else MaterialTheme.colors.onBackground
        )

        if (fps > 0) {
            Text(
                text = "FPS: $fps",
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}

@Composable
fun BoxScope.drawControlPanel(
    landerState: LanderState,
    onControlInputs: (ControlInputs) -> Unit,
) {
    // Small button on the bottom right of the screen
    if (landerState.status == GameStatus.PLAYING) {
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

            Button(
                onClick = { /* Handled by interaction source */ },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (landerState.isThrusting)
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

            onControlInputs(
                ControlInputs(
                    thrust = isThrustPressed,
                    rotateRight = isRotateRightPressed,
                    rotateLeft = isRotateLeftPressed,
                )
            )
        }
    }
}

@Composable
fun BoxScope.GameOverContent(
    state: GameScreenState.GameOver,
    onRestartClicked: () -> Unit,
    onBackToStartClicked: () -> Unit
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawStars(state.environmentState.config)
        drawTerrain(state.environmentState.terrain)
        drawLandingPads(state.environmentState.terrain)
        drawLander(state.landerState)
    }
    drawInfoPanel(state.landerState, 0)
    GameOverMessage(
        uiState = state,
        onRestartClicked = onRestartClicked,
        onBackToStartClicked = onBackToStartClicked
    )
}

@Composable
fun GameOverMessage(
    uiState: GameScreenState.GameOver,
    onRestartClicked: () -> Unit,
    onBackToStartClicked: () -> Unit
) {
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
                    text = if (uiState.isSuccess) "MISSION SUCCESSFUL" else "MISSION FAILED",
                    style = MaterialTheme.typography.h5,
                    color = if (uiState.isSuccess)
                        Color.Green
                    else
                        Color.Red,
                    textAlign = TextAlign.Center
                )

                // Result message
                Text(
                    text = uiState.message,
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