package com.balch.lander.screens.gameplay

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.balch.lander.CameraZoomLevel
import com.balch.lander.GameConfig
import com.balch.lander.core.game.Camera
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.TerrainGeneratorImpl
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.utils.FontScaler
import com.balch.lander.core.utils.StringFormatter
import com.balch.lander.core.utils.impl.KotlinxDateTimeProvider
import com.balch.lander.screens.gameplay.GamePlayViewModel.GameScreenState
import com.balch.lander.screens.gameplay.widgets.DebugOverlay
import com.balch.lander.screens.gameplay.widgets.DrawControlPanel
import com.balch.lander.screens.gameplay.widgets.DrawInfoPanel
import com.balch.lander.screens.gameplay.widgets.GameCanvas
import com.balch.lander.screens.gameplay.widgets.utils.toDp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Game Screen for the Lunar Lander game.
 * Displays the game area, lander information, and controls.
 */
@Composable
fun GamePlayScreen(
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

    var lastControlInputs by remember { mutableStateOf(ControlInputs()) }

    val fontScaler = FontScaler(1f)
    val stringFormatter = StringFormatter()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Animate camera scale changes
    val animatedScaleX by animateFloatAsState(
        targetValue = state.camera.zoomLevel.scale,
        animationSpec = tween(durationMillis = 500),
        label = "scaleX"
    )
    val animatedScaleY by animateFloatAsState(
        targetValue = state.camera.zoomLevel.scale,
        animationSpec = tween(durationMillis = 500),
        label = "scaleY"
    )

    // Animate camera offset changes
    val animatedOffsetX by animateFloatAsState(
        targetValue = state.camera.offset.x,
        animationSpec = tween(durationMillis = 500),
        label = "offsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = state.camera.offset.y,
        animationSpec = tween(durationMillis = 500),
        label = "offsetY"
    )

    // Calculate offset in dp
    val (offsetXDp, offsetYDp) = toDp(
        point = Vector2D(animatedOffsetX, animatedOffsetY),
        config = state.environment.config,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable(true)
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                val controlInputs = KeyEventProcessor.handleEvent(event, lastControlInputs)
                if (lastControlInputs != controlInputs) {
                    lastControlInputs = controlInputs
                    onControlInputs(controlInputs)
                }
                false
            }
    ) {
        GameCanvas(
            modifier = Modifier.fillMaxSize()
                .scale(animatedScaleX, animatedScaleY)
                .offset(offsetXDp, -offsetYDp),
            landerState = state.landerState,
            terrain = state.environment.terrain,
            config = state.environment.config,
        )
        DrawInfoPanel(state.landerState, fontScaler, stringFormatter)

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 44.dp)
                .safeDrawingPadding(),
        ) {
            DrawControlPanel(
                modifier = Modifier.align(Alignment.End),
                landerState = state.landerState,
                onControlInputs = onControlInputs,
                fontScaler = fontScaler
            )

            DebugOverlay(
                modifier = Modifier.align(Alignment.End),
                landerState = state.landerState,
                camera = state.camera,
                score = state.currentScore,
                gameTimeSecs = state.gameTimeSeconds.toInt(),
                fps = state.fps,
                platform = state.environment.platform.name,
                fontScaler = fontScaler,
                stringFormatter = stringFormatter,
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
    GameCanvas(
        modifier = Modifier.fillMaxSize(),
        landerState = state.landerState,
        terrain = state.environmentState.terrain,
        config = state.environmentState.config,
    )
    DrawInfoPanel(state.landerState)
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

                // Score message
                Text(
                    text = "Score: ${uiState.finalScore} Time: ${uiState.timeTaken.toInt()}s",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
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

private val previewConfig: GameConfig by lazy {  GameConfig() }

private val previewTerrain by lazy {
    TerrainGeneratorImpl(KotlinxDateTimeProvider())
        .generateTerrain(previewConfig.screenWidth, previewConfig.screenHeight)
}

@Preview
@Composable
fun PlayingContentPreview() {
    val landerState = LanderState(
        position = Vector2D(500f, 100f),
        thrustStrength = ThrustStrength.HIGH,
        rotation = 30f,
    )
    PlayingContentPreviewWrapper(landerState)
}

@Preview
@Composable
fun PlayingContentZoomMediumPreview() {
    val distanceToSeaLevel = CameraZoomLevel.FAR.distanceThreshold - 1
    val landerState = LanderState(
        position = Vector2D(
            x = 500f,
            y = previewConfig.screenHeight - distanceToSeaLevel
        ),
        distanceToSeaLevel = distanceToSeaLevel
    )
    PlayingContentPreviewWrapper(landerState)
}

@Preview
@Composable
fun PlayingContentZoomMediumLeftPreview() {
    val distanceToSeaLevel = CameraZoomLevel.FAR.distanceThreshold - 1
    val landerState = LanderState(
        position = Vector2D(
            x = 200f,
            y = previewConfig.screenHeight - distanceToSeaLevel
        ),
        distanceToSeaLevel = distanceToSeaLevel
    )
    PlayingContentPreviewWrapper(landerState)
}

@Preview
@Composable
fun PlayingContentZoomMediumRightPreview() {
    val distanceToSeaLevel = CameraZoomLevel.FAR.distanceThreshold - 1
    val landerState = LanderState(
        position = Vector2D(
            x = previewConfig.screenWidth - 200f,
            y = previewConfig.screenHeight - distanceToSeaLevel
        ),
        distanceToSeaLevel = distanceToSeaLevel
    )
    PlayingContentPreviewWrapper(landerState)
}

@Preview
@Composable
fun PlayingContentZoomClosePreview() {
    val distanceToSeaLevel = CameraZoomLevel.MEDIUM.distanceThreshold - 1
    val landerState = LanderState(
        position = Vector2D(
            x = 500f,
            y = previewConfig.screenHeight - distanceToSeaLevel
        ),
        distanceToSeaLevel = distanceToSeaLevel,
        flightStatus = FlightStatus.DANGER,
    )
    PlayingContentPreviewWrapper(landerState)
}

@Preview
@Composable
fun PlayingContentZoomCloseLeftPreview() {
    val distanceToSeaLevel = CameraZoomLevel.MEDIUM.distanceThreshold - 1
    val landerState = LanderState(
        position = Vector2D(
            x = 100f,
            y = previewConfig.screenHeight - distanceToSeaLevel
        ),
        distanceToSeaLevel = distanceToSeaLevel,
        flightStatus = FlightStatus.DANGER,
    )
    PlayingContentPreviewWrapper(landerState)
}

@Preview
@Composable
fun PlayingContentZoomCloseRightPreview() {
    val distanceToSeaLevel = CameraZoomLevel.MEDIUM.distanceThreshold - 1
    val landerState = LanderState(
        position = Vector2D(
            x = previewConfig.screenWidth - 100f,
            y = previewConfig.screenHeight - distanceToSeaLevel
        ),
        distanceToSeaLevel = distanceToSeaLevel
    )
    PlayingContentPreviewWrapper(landerState)
}

@Composable
fun PlayingContentPreviewWrapper(
    landerState: LanderState,
) {
    val camera = Camera.calculateCameraInfo(landerState, previewConfig)

    val state = GameScreenState.Playing(
        landerState = landerState,
        environment = GameEnvironmentState(previewTerrain, previewConfig),
        fps = 60,
        camera = camera,
    )

    CompositionLocalProvider(
        LocalDensity provides Density(1.75f, 1f)
    ) {
        val (width, height) = toDp(Vector2D(previewConfig.screenWidth, previewConfig.screenHeight), previewConfig)
        MaterialTheme(colors = darkColors()) {
            Box(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .background(Color.Black)
            ) {
                PlayingContent(state = state, onControlInputs = {})
            }
        }
    }
}

