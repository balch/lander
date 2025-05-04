package com.balch.lander.core.utils.impl

import com.balch.lander.core.utils.TimeProvider

/**
 * Platform-agnostic utility for handling time.
 */
expect class TimeProviderImpl constructor(): TimeProvider {
    /**
     * Gets the current time in milliseconds.
     * @return Current time in milliseconds
     */
    override fun currentTimeMillis(): Long

    override val isTimeAccurate: Boolean
}