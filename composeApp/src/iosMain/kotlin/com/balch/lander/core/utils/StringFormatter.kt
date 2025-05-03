package com.balch.lander.core.utils

import kotlin.math.min

/**
 * Platform-agnostic utility for handling String.
 */
actual class StringFormatter {
    actual fun formatToString(value: Double): String =
        value.toString().let {
            val pos = it.indexOf(".")
            if (pos != -1) {
                it.substring(0, min(pos + 3, it.length - pos + 1))
            } else it
        }

    actual fun formatToString(value: Float): String =
        formatToString(value.toDouble())
}