package com.balch.lander.core.utils

/**
 * Platform-agnostic utility for handling time.
 */
expect object TimeUtil {
    /**
     * Gets the current time in milliseconds.
     * @return Current time in milliseconds
     */
    fun currentTimeMillis(): Long
}