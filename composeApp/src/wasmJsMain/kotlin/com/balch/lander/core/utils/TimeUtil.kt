package com.balch.lander.core.utils

/**
 * Default Native implementation of TimeUtil.
 */
actual object TimeUtil {

    actual val isTimeAccurate: Boolean = false

    // Counter for incremental time
    private var counter = 0L

    /**
     * Gets the current time in milliseconds.
     * For WebAssembly, we use a simple counter that increments with each call.
     * This is sufficient for calculating delta time in the game loop.
     * @return Current time in milliseconds
     */
    actual fun currentTimeMillis(): Long {
        counter += 16 // Simulate ~60 FPS
        return counter
    }
}