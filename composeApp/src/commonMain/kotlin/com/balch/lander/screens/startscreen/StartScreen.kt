package com.balch.lander.screens.startscreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.balch.lander.GameConfig
import com.balch.lander.GravityLevel
import com.balch.lander.LandingPadSize
import com.balch.lander.ThrustStrength
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

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
    onThrustStrengthChanged: (ThrustStrength) -> Unit,
    onStartGameClicked: () -> Unit
) {
    // Define retro colors
    val retroBackground = Color(0xFF000020) // Dark blue background
    val retroGreen = Color(0xFF00FF00) // Bright green for text
    val retroYellow = Color(0xFFFFFF00) // Yellow for highlights
    val retroCyan = Color(0xFF00FFFF) // Cyan for accents
    val retroBlue = Color(0xFF0088FF) // Blue for buttons

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(retroBackground)
    ) {
        // Stars background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = Random(42) // Fixed seed for consistent star pattern
            repeat(100) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val radius = random.nextFloat() * 2f + 1f
                drawCircle(
                    color = Color.White.copy(alpha = random.nextFloat() * 0.7f + 0.3f),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }

        // Use Row as the main container for landscape orientation
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left side: Title and Instructions
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, retroCyan, RoundedCornerShape(4.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LUNAR LANDER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = retroYellow,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Instructions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF101040))
                        .border(1.dp, retroGreen, RoundedCornerShape(4.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "INSTRUCTIONS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = retroCyan,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )

                        Text(
                            text = "Land your spacecraft safely on the lunar surface.",
                            fontFamily = FontFamily.Monospace,
                            color = retroGreen,
                            fontSize = MaterialTheme.typography.caption.fontSize
                        )

                        Text(
                            text = "• LEFT/RIGHT: rotate",
                            fontFamily = FontFamily.Monospace,
                            color = retroGreen,
                            fontSize = MaterialTheme.typography.caption.fontSize
                        )

                        Text(
                            text = "• THRUST: slow descent",
                            fontFamily = FontFamily.Monospace,
                            color = retroGreen,
                            fontSize = MaterialTheme.typography.caption.fontSize
                        )

                        Text(
                            text = "• LAND GENTLY on pads",
                            fontFamily = FontFamily.Monospace,
                            color = retroGreen,
                            fontSize = MaterialTheme.typography.caption.fontSize
                        )
                    }
                }

                // Start Button
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .height(36.dp)
                        .fillMaxWidth(0.8f)
                        .align(Alignment.CenterHorizontally)
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
                        fontSize = MaterialTheme.typography.button.fontSize
                    )
                }
            }

            // Right side: Configuration Options and Start Button
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Configuration Options
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF101040))
                        .border(1.dp, retroCyan, RoundedCornerShape(4.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "GAME OPTIONS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = retroYellow,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )

                        // Fuel Level Slider
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "FUEL LEVEL",
                                fontFamily = FontFamily.Monospace,
                                color = retroCyan,
                                fontSize = MaterialTheme.typography.caption.fontSize
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LOW",
                                    fontFamily = FontFamily.Monospace,
                                    color = retroGreen,
                                    fontSize = MaterialTheme.typography.caption.fontSize
                                )

                                Slider(
                                    value = uiState.gameConfig.fuelLevel,
                                    onValueChange = onFuelLevelChanged,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
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
                                    fontSize = MaterialTheme.typography.caption.fontSize
                                )
                            }
                        }

                        // Gravity Level Options
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "GRAVITY LEVEL",
                                fontFamily = FontFamily.Monospace,
                                color = retroCyan,
                                fontSize = MaterialTheme.typography.caption.fontSize
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                GravityLevel.values().forEach { gravityLevel ->
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
                                            text = gravityLevel.label.take(1),
                                            fontFamily = FontFamily.Monospace,
                                            color = if (uiState.gameConfig.gravity == gravityLevel)
                                                Color.White
                                            else
                                                retroGreen,
                                            fontSize = MaterialTheme.typography.caption.fontSize,
                                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Landing Pad Size Options
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "LANDING PAD SIZE",
                                fontFamily = FontFamily.Monospace,
                                color = retroCyan,
                                fontSize = MaterialTheme.typography.caption.fontSize
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                LandingPadSize.values().forEach { padSize ->
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
                                            text = padSize.label.take(1),
                                            fontFamily = FontFamily.Monospace,
                                            color = if (uiState.gameConfig.landingPadSize == padSize)
                                                Color.White
                                            else
                                                retroGreen,
                                            fontSize = MaterialTheme.typography.caption.fontSize,
                                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Thrust Strength Options
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "THRUST STRENGTH",
                                fontFamily = FontFamily.Monospace,
                                color = retroCyan,
                                fontSize = MaterialTheme.typography.caption.fontSize
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ThrustStrength.values().forEach { thrustStrength ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 1.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (uiState.gameConfig.thrustStrength == thrustStrength)
                                                    retroBlue
                                                else
                                                    Color(0xFF202060)
                                            )
                                            .border(
                                                1.dp,
                                                if (uiState.gameConfig.thrustStrength == thrustStrength)
                                                    retroYellow
                                                else
                                                    retroCyan,
                                                RoundedCornerShape(2.dp)
                                            )
                                            .clickable { onThrustStrengthChanged(thrustStrength) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = thrustStrength.label.take(1),
                                            fontFamily = FontFamily.Monospace,
                                            color = if (uiState.gameConfig.thrustStrength == thrustStrength)
                                                Color.White
                                            else
                                                retroGreen,
                                            fontSize = MaterialTheme.typography.caption.fontSize,
                                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
            thrustStrength = ThrustStrength.MEDIUM
        ),
        navigateToGame = false
    )

    MaterialTheme(colors = darkColors()) {
        Box(
            modifier = Modifier
                .width(640.dp)
                .height(340.dp)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            StartScreen(
                uiState = defaultState,
                onFuelLevelChanged = {},
                onGravityLevelChanged = {},
                onLandingPadSizeChanged = {},
                onThrustStrengthChanged = {},
                onStartGameClicked = {}
            )
        }
    }
}
