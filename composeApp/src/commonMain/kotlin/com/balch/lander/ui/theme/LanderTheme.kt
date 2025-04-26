package com.balch.lander.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define colors for the dark theme (primary theme for the game)
private val DarkColorPalette = darkColors(
    primary = Color(0xFF80DEEA),       // Light blue
    primaryVariant = Color(0xFF4BACB8), // Darker blue
    secondary = Color(0xFFFFD54F),     // Amber
    background = Color(0xFF121212),    // Very dark gray
    surface = Color(0xFF1E1E1E),       // Dark gray
    onPrimary = Color(0xFF000000),     // Black
    onSecondary = Color(0xFF000000),   // Black
    onBackground = Color(0xFFFFFFFF),  // White
    onSurface = Color(0xFFFFFFFF)      // White
)

// Define colors for the light theme (alternative theme)
private val LightColorPalette = lightColors(
    primary = Color(0xFF26C6DA),       // Cyan
    primaryVariant = Color(0xFF0095A8), // Darker cyan
    secondary = Color(0xFFFFB300),     // Amber
    background = Color(0xFFF5F5F5),    // Light gray
    surface = Color(0xFFFFFFFF),       // White
    onPrimary = Color(0xFFFFFFFF),     // White
    onSecondary = Color(0xFF000000),   // Black
    onBackground = Color(0xFF000000),  // Black
    onSurface = Color(0xFF000000)      // Black
)

/**
 * Theme for the Lunar Lander game.
 * Uses dark theme by default as specified in the guidelines.
 */
@Composable
fun LanderTheme(
    darkTheme: Boolean = true, // Force dark theme by default
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}