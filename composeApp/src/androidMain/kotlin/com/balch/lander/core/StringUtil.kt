package com.balch.lander.core

import android.annotation.SuppressLint

/**
 * Platform-agnostic utility for handling String.
 */
actual object StringUtil {
    @SuppressLint("DefaultLocale")
    actual fun formatToString(value: Double): String =
        String.format("%.02f", value)

    actual fun formatToString(value: Float): String =
        formatToString(value.toDouble())
}