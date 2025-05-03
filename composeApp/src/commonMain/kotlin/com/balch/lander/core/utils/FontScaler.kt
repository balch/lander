package com.balch.lander.core.utils

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

class FontScaler(val scale: Float) {

    /**
     *
     * TODO: this needs some work
     * Calculates the appropriate font scale based on the provided screen dimensions.
     *
     * @param screenWidth The width of the screen in pixels.
     * @param screenHeight The height of the screen in pixels.
     * @return A font scale value constrained between 0.6 and 1.2, reflecting the scaling factor for font sizes.
     */
    constructor(screenWidth: Float, screenHeight: Float) : this(
        minOf(
            screenWidth / 383f,
            screenHeight / 832f,
            1.2f
        ).coerceAtLeast(0.7f)
    )

    /**
     * Extension function to scale TextUnit values with a factor
     */
    fun scale(textUnit: TextUnit): TextUnit =
        (textUnit.value * scale).sp
}