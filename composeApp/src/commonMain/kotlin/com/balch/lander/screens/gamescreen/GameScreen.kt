package com.balch.lander.screens.gamescreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balch.lander.GameConfig
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.utils.FontScaler
import com.balch.lander.core.utils.StringFormatter
import org.jetbrains.compose.ui.tooling.preview.Preview
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
fun PlayingContent(
    state: GameScreenState.Playing,
    onControlInputs: (ControlInputs) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    var isThrustPressed = remember { false }
    var isRotateLeftPressed = remember { false }
    var isRotateRightPressed = remember { false }
    val lastControlInputs = remember { mutableStateOf<ControlInputs?>(null) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable(true)
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionUp -> isThrustPressed = true
                        Key.DirectionRight -> isRotateRightPressed = true
                        Key.DirectionLeft -> isRotateLeftPressed = true
                    }
                } else if (event.type == KeyEventType.KeyUp) {
                    when (event.key) {
                        Key.DirectionUp -> isThrustPressed = false
                        Key.DirectionRight -> isRotateRightPressed = false
                        Key.DirectionLeft -> isRotateLeftPressed = false
                    }
                }

                val controlInputs = ControlInputs(
                    thrustStrength =
                        if (isThrustPressed) ThrustStrength.MEDIUM
                        else ThrustStrength.OFF,
                    rotateRight = isRotateRightPressed,
                    rotateLeft = isRotateLeftPressed,
                )
                if (lastControlInputs.value != controlInputs) {
                    lastControlInputs.value = controlInputs
                    onControlInputs(controlInputs)
                    true
                } else {
                    false
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawStars(state.environmentState.config)
            drawTerrain(state.environmentState.terrain)
            drawLandingPads(state.environmentState.terrain)
            drawLander(
                state.landerState,
                state.environmentState.config.landerSize
            )
        }
        drawInfoPanel(state.landerState, state.fps)
        drawControlPanel(state.landerState, onControlInputs)
    }
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

fun DrawScope.drawLander(landerState: LanderState, landerSize: Float) {
    // Draw lander
    val landerX = landerState.position.x / 1000f * size.width
    val landerY = landerState.position.y / 1000f * size.height

    val isThrusting = landerState.thrustStrength.value > 0f

    // Determine lander color based on game state
    val landerColor = when {
        landerState.isDangerMode -> Color.Red
        isThrusting -> Color.Yellow
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
        if (isThrusting && landerState.fuel > 0) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(landerX - landerSize / 4, landerY + landerSize / 2),
                size = Size(landerSize / 2, landerSize * landerState.thrustStrength.value),
                alpha = 0.5f
            )
        }
    }
}

@Composable
fun BoxScope.drawInfoPanel(landerState: LanderState, fps: Int) {
    val fontScaleFactor = FontScaler(1f)
    val stringFormatter = StringFormatter()

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
            fontSize = fontScaleFactor.scale(14.sp),
        )

        Text(
            text = "DESCENT: ${abs(landerState.velocity.y).toInt()} m/s",
            color = if (abs(landerState.velocity.y) > 3) Color.Red else MaterialTheme.colors.onBackground,
            fontSize = fontScaleFactor.scale(12.sp),
        )

        Text(
            text = "DRIFT: ${stringFormatter.formatToString(landerState.velocity.x)} m/s",
            color = if (abs(landerState.velocity.x) > 2) Color.Red else MaterialTheme.colors.onBackground,
            fontSize = fontScaleFactor.scale(12.sp),
        )

        Text(
            text = "ALTITUDE: ${landerState.distanceToGround.toInt()} m",
            color = if (landerState.distanceToGround < 50) Color.Red else MaterialTheme.colors.onBackground,
            fontSize = fontScaleFactor.scale(12.sp),
        )

        if (fps > 0) {
            Text(
                text = "FPS: $fps",
                color = MaterialTheme.colors.onBackground,
                fontSize = fontScaleFactor.scale(12.sp),
            )
        }
    }
}

@Composable
fun BoxScope.drawControlPanel(
    landerState: LanderState,
    onControlInputs: (ControlInputs) -> Unit,
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
                        style = MaterialTheme.typography.body2,
                        color = Color.White,
                        textAlign = TextAlign.Center
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
                        style = MaterialTheme.typography.body2,
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
                        style = MaterialTheme.typography.body2,
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
                        style = MaterialTheme.typography.h6,
                        color = Color.White,
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
                    interactionSource = rotateRightInteractionSource
                ) {
                    Text(
                        text = "-->",
                        style = MaterialTheme.typography.h6,
                        color = Color.White
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
        drawLander(
            state.landerState,
            state.environmentState.config.landerSize
        )
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

@Preview
@Composable
fun LanderPreview() {
    val landerState = LanderState()

    MaterialTheme(colors = darkColors()) {
        Canvas(modifier = Modifier
            .width(20.dp)
            .height(20.dp)
            .offset(10.dp, 10.dp)) {
            drawLander(landerState, 25F)
        }
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

@Preview
@Composable
fun ControlPadPreview() {
    val landerState = LanderState()

    MaterialTheme(colors = darkColors()) {
        Box(modifier = Modifier
            .width(600.dp)
            .height(350.dp)
        ) {
            drawControlPanel(landerState, {  })
        }
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
            drawInfoPanel(landerState, 60)
        }
    }
}
