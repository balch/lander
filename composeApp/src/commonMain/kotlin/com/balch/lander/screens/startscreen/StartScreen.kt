package com.balch.lander.screens.startscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.balch.lander.GravityLevel
import com.balch.lander.LandingPadSize
import com.balch.lander.ThrustStrength

/**
 * Start Screen for the Lunar Lander game.
 * Displays game title, instructions, and configuration options.
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
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Stars background would go here
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "LUNAR LANDER",
                style = MaterialTheme.typography.h3,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "INSTRUCTIONS",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary
                    )
                    
                    Text(
                        text = "Land your spacecraft safely on the lunar surface. Control your descent using thrusters and manage your fuel carefully.",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                    
                    Text(
                        text = "• Use LEFT and RIGHT to rotate the lander",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface
                    )
                    
                    Text(
                        text = "• Use THRUST to slow your descent",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface
                    )
                    
                    Text(
                        text = "• Land gently on the landing pads with proper orientation",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
            
            // Configuration Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "GAME OPTIONS",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary
                    )
                    
                    // Fuel Level Slider
                    Column {
                        Text(
                            text = "Fuel Level",
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.onSurface
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Low",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface
                            )
                            
                            Slider(
                                value = uiState.gameConfig.fuelLevel,
                                onValueChange = onFuelLevelChanged,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            )
                            
                            Text(
                                text = "High",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                    }
                    
                    // Gravity Level Options
                    Column {
                        Text(
                            text = "Gravity Level",
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.onSurface
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GravityLevel.values().forEach { gravityLevel ->
                                Button(
                                    onClick = { onGravityLevelChanged(gravityLevel) },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (uiState.gameConfig.gravity == gravityLevel) 
                                            MaterialTheme.colors.primary 
                                        else 
                                            MaterialTheme.colors.surface
                                    ),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(
                                        text = gravityLevel.label,
                                        color = if (uiState.gameConfig.gravity == gravityLevel) 
                                            MaterialTheme.colors.onPrimary 
                                        else 
                                            MaterialTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                    
                    // Landing Pad Size Options
                    Column {
                        Text(
                            text = "Landing Pad Size",
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.onSurface
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LandingPadSize.values().forEach { padSize ->
                                Button(
                                    onClick = { onLandingPadSizeChanged(padSize) },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (uiState.gameConfig.landingPadSize == padSize) 
                                            MaterialTheme.colors.primary 
                                        else 
                                            MaterialTheme.colors.surface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = padSize.label,
                                        color = if (uiState.gameConfig.landingPadSize == padSize) 
                                            MaterialTheme.colors.onPrimary 
                                        else 
                                            MaterialTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                    
                    // Thrust Strength Options
                    Column {
                        Text(
                            text = "Thrust Strength",
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.onSurface
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ThrustStrength.values().forEach { thrustStrength ->
                                Button(
                                    onClick = { onThrustStrengthChanged(thrustStrength) },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (uiState.gameConfig.thrustStrength == thrustStrength) 
                                            MaterialTheme.colors.primary 
                                        else 
                                            MaterialTheme.colors.surface
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = thrustStrength.label,
                                        color = if (uiState.gameConfig.thrustStrength == thrustStrength) 
                                            MaterialTheme.colors.onPrimary 
                                        else 
                                            MaterialTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Start Button
            Button(
                onClick = onStartGameClicked,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.secondary
                ),
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .height(56.dp)
                    .fillMaxWidth(0.7f)
            ) {
                Text(
                    text = "START MISSION",
                    style = MaterialTheme.typography.button,
                    color = MaterialTheme.colors.onSecondary
                )
            }
        }
    }
}