package com.balch.lander.core.utils

import android.annotation.SuppressLint

/**
 * Android utility for handling String.
 */
actual class StringFormatter {
    @SuppressLint("DefaultLocale")
    actual fun formatToString(value: Double): String =
        String.format("%.2f", value)

    actual fun formatToString(value: Float): String =
        formatToString(value.toDouble())
}