package com.balch.lander.screens.startscreen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balch.lander.GameConfig
import com.balch.lander.GravityLevel
import com.balch.lander.LandingPadSize
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Preview function for the Start Screen.
 * This provides a preview of the Start Screen with default configuration.
 * The UI is designed for landscape orientation.
 * Note: When viewing in Android Studio, use the landscape orientation in the preview tool.
 */
@Preview
@Composable
fun StartScreenPreview() {
    val defaultState = StartScreenState(
        gameConfig = GameConfig(
            fuelLevel = 0.5f,
            gravity = GravityLevel.MEDIUM,
            landingPadSize = LandingPadSize.MEDIUM,
        )
    )

    MaterialTheme(colors = darkColors()) {
        Box(
            modifier = Modifier
                .width(384.dp)
                .height(832.dp)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            StartScreen(
                uiState = defaultState,
                onFuelLevelChanged = {},
                onGravityLevelChanged = {},
                onLandingPadSizeChanged = {},
                onStartGameClicked = {}
            )
        }
    }
}

@Preview
@Composable
fun StartScreenPreviewLandscape() {
    val defaultState = StartScreenState(
        gameConfig = GameConfig(
            fuelLevel = 0.5f,
            gravity = GravityLevel.MEDIUM,
            landingPadSize = LandingPadSize.MEDIUM,
        )
    )

    MaterialTheme(colors = darkColors()) {
        Box(
            modifier = Modifier
                .width(832.dp)
                .height(384.dp)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            StartScreen(
                uiState = defaultState,
                onFuelLevelChanged = {},
                onGravityLevelChanged = {},
                onLandingPadSizeChanged = {},
                onStartGameClicked = {}
            )
        }
    }
}

/**
 * Start Screen for the Lunar Lander game.
 * Displays game title, instructions, and configuration options.
 * Optimized for landscape orientation to fit on a single screen.
 */
@Composable
fun StartScreen(
    uiState: StartScreenState,
    onFuelLevelChanged: (Float) -> Unit,
    onGravityLevelChanged: (GravityLevel) -> Unit,
    onLandingPadSizeChanged: (LandingPadSize) -> Unit,
    onStartGameClicked: () -> Unit
) {
    // Define retro colors (unchanged)
    val retroBackground = Color(0xFF000020) // Dark blue background
    val retroGreen = Color(0xFF00FF00) // Bright green for text
    val retroYellow = Color(0xFFFFFF00) // Yellow for highlights
    val retroCyan = Color(0xFF00FFFF) // Cyan for accents
    val retroBlue = Color(0xFF0088FF) // Blue for buttons

    // Use BoxWithConstraints to get screen dimensions
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(retroBackground)
    ) {
        // Calculate available space
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val isPortrait = availableWidth < availableHeight

        // Calculate font scale factor based on screen size
        // This helps adapt to user font size preferences
        val fontScaleFactor = minOf(
            availableWidth.value / 383f,
            availableHeight.value / 832f,
            1.2f
        ).coerceAtLeast(0.6f)

        // Calculate dynamic spacing based on available space
        val dynamicSpacing = (8 * fontScaleFactor).dp
        val smallPadding = (4 * fontScaleFactor).dp

        // Create responsive composable functions
        @Composable
        fun RenderTitle() {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, retroCyan, RoundedCornerShape(4.dp))
                    .padding(smallPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VIBE LANDER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = retroYellow,
                    textAlign = TextAlign.Center,
                    fontSize = (18 * fontScaleFactor).sp,
                    modifier = Modifier.padding(vertical = smallPadding)
                )
            }
        }

        @Composable
        fun RenderInstructions() {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF101040))
                    .border(1.dp, retroGreen, RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier.padding(smallPadding),
                    verticalArrangement = Arrangement.spacedBy(smallPadding)
                ) {
                    Text(
                        text = "INSTRUCTIONS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = retroCyan,
                        fontSize = (14 * fontScaleFactor).sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // Conditionally show instructions based on available space
                    val instructionFontSize = (12 * fontScaleFactor).sp

                    Text(
                        text = "Land your spacecraft safely on the lunar surface.",
                        fontFamily = FontFamily.Monospace,
                        color = retroGreen,
                        fontSize = instructionFontSize
                    )

                    // In very constrained spaces, show fewer instruction items
                    if (isPortrait || availableHeight > 300.dp) {
                        Text(
                            text = "• LEFT/RIGHT: rotate",
                            fontFamily = FontFamily.Monospace,
                            color = retroGreen,
                            fontSize = instructionFontSize
                        )

                        Text(
                            text = "• THRUST: slow descent",
                            fontFamily = FontFamily.Monospace,
                            color = retroGreen,
                            fontSize = instructionFontSize
                        )

                        Text(
                            text = "• LAND GENTLY on pads",
                            fontFamily = FontFamily.Monospace,
                            color = retroGreen,
                            fontSize = instructionFontSize
                        )
                    }
                }
            }
        }

        @Composable
        fun RenderStartButton() {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(smallPadding),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = smallPadding)
                        .height(40.dp)
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(retroBlue)
                        .border(2.dp, retroYellow, RoundedCornerShape(4.dp))
                        .clickable(onClick = onStartGameClicked),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "START MISSION",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = (14 * fontScaleFactor).sp
                    )
                }
            }
        }

        @Composable
        fun RenderConfigOptions() {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF101040))
                    .border(1.dp, retroCyan, RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier.padding(smallPadding),
                    verticalArrangement = Arrangement.spacedBy(smallPadding)
                ) {
                    Text(
                        text = "GAME OPTIONS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = retroYellow,
                        fontSize = (14 * fontScaleFactor).sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // Fuel Level Slider
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "FUEL LEVEL",
                            fontFamily = FontFamily.Monospace,
                            color = retroCyan,
                            fontSize = (12 * fontScaleFactor).sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOW",
                                fontFamily = FontFamily.Monospace,
                                color = retroGreen,
                                fontSize = (12 * fontScaleFactor).sp
                            )

                            Slider(
                                value = uiState.gameConfig.fuelLevel,
                                onValueChange = onFuelLevelChanged,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = smallPadding),
                                colors = SliderDefaults.colors(
                                    thumbColor = retroYellow,
                                    activeTrackColor = retroGreen,
                                    inactiveTrackColor = Color(0xFF005000)
                                )
                            )

                            Text(
                                text = "HIGH",
                                fontFamily = FontFamily.Monospace,
                                color = retroGreen,
                                fontSize = (12 * fontScaleFactor).sp
                            )
                        }
                    }

                    // Gravity Level Options with responsive layout
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "GRAVITY LEVEL",
                            fontFamily = FontFamily.Monospace,
                            color = retroCyan,
                            fontSize = (12 * fontScaleFactor).sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GravityLevel.entries.forEach { gravityLevel ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 1.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (uiState.gameConfig.gravity == gravityLevel)
                                                retroBlue
                                            else
                                                Color(0xFF202060)
                                        )
                                        .border(
                                            1.dp,
                                            if (uiState.gameConfig.gravity == gravityLevel)
                                                retroYellow
                                            else
                                                retroCyan,
                                            RoundedCornerShape(2.dp)
                                        )
                                        .clickable { onGravityLevelChanged(gravityLevel) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = gravityLevel.label,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (uiState.gameConfig.gravity == gravityLevel)
                                            Color.White
                                        else
                                            retroGreen,
                                        fontSize = (12 * fontScaleFactor).sp,
                                        modifier = Modifier.padding(
                                            vertical = (2 * fontScaleFactor).dp,
                                            horizontal = fontScaleFactor.dp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Similar adaptations for Landing Pad Size and Thrust Strength
                    // (code omitted for brevity but follows the same pattern)

                    // Landing Pad Size Options
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "LANDING PAD SIZE",
                            fontFamily = FontFamily.Monospace,
                            color = retroCyan,
                            fontSize = (12 * fontScaleFactor).sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LandingPadSize.entries.forEach { padSize ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 1.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (uiState.gameConfig.landingPadSize == padSize)
                                                retroBlue
                                            else
                                                Color(0xFF202060)
                                        )
                                        .border(
                                            1.dp,
                                            if (uiState.gameConfig.landingPadSize == padSize)
                                                retroYellow
                                            else
                                                retroCyan,
                                            RoundedCornerShape(2.dp)
                                        )
                                        .clickable { onLandingPadSizeChanged(padSize) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = padSize.label,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (uiState.gameConfig.landingPadSize == padSize)
                                            Color.White
                                        else
                                            retroGreen,
                                        fontSize = (12 * fontScaleFactor).sp,
                                        modifier = Modifier.padding(
                                            vertical = 2.dp,
                                            horizontal = 1.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Optimized Landscape Layout
        @Composable
        fun RenderLandscape() {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dynamicSpacing),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dynamicSpacing)
            ) {
                // Left side: Title and Instructions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(dynamicSpacing)
                ) {
                    RenderTitle()
                    RenderInstructions()
                    RenderStartButton()

                    // Add spacer to push content to the top
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Right side: Configuration Options and Start Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(dynamicSpacing)
                ) {
                    RenderConfigOptions()
                }
            }
        }

        // Optimized Portrait Layout with scrolling capability
        @Composable
        fun RenderPortrait() {
            // Use scrollable column to handle overflow with large font sizes
            androidx.compose.foundation.rememberScrollState().let { scrollState ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            dynamicSpacing,
                            WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                            dynamicSpacing,
                            dynamicSpacing
                        )
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(dynamicSpacing)
                ) {
                    RenderTitle()

                    // For very small screens, we can conditionally show/hide instructions
                    if (availableHeight > 500.dp) {
                        RenderInstructions()
                    }

                    RenderConfigOptions()
                    RenderStartButton()

                    // Add padding at the bottom for scrolling
                    Spacer(modifier = Modifier.height(dynamicSpacing))
                }
            }
        }

        // Render the appropriate layout based on orientation
        if (isPortrait) {
            RenderPortrait()
        } else {
            RenderLandscape()
        }
    }
}
