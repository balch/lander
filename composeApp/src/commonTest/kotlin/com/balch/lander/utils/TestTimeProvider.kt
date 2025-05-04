package com.balch.lander.utils

import com.balch.lander.core.utils.TimeProvider

/**
 * Provides a test implementation of [TimeProvider] that allows controlled manipulation of time.
 * Useful for scenarios where temporal states need to be tested deterministically.
 *
 * @param currentTimeMillis Initial value for the current time in milliseconds. Defaults to 0.
 * @param isTimeAccurate Indicates whether the time is considered accurate. Defaults to false.
 */
class TestTimeProvider(
    private var currentTimeMillis: Long = 0L,
    override val isTimeAccurate: Boolean = false,
): TimeProvider {
    override fun currentTimeMillis(): Long = currentTimeMillis
    fun advanceTimeBy(millis: Long = 16) {
        currentTimeMillis += millis
    }
}