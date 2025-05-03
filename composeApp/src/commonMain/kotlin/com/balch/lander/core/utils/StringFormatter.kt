package com.balch.lander.core.utils

/**
 * Platform-agnostic utility for handling String.
 */
expect class StringFormatter constructor() {
    fun formatToString(value: Double): String
    fun formatToString(value: Float): String
}