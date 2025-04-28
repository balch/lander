package com.balch.lander.core

/**
 * Platform-agnostic utility for handling String.
 */
expect object StringUtil {
    fun formatToString(value: Double): String
    fun formatToString(value: Float): String
}