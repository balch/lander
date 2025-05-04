package com.balch.lander.core.utils.impl

import com.balch.lander.core.utils.TimeProvider

/**
 * JVM/Desktop implementation of TimeUtil.
 */
actual class TimeProviderImpl: TimeProvider {

    actual override val isTimeAccurate: Boolean = true

    /**
     * Gets the current time in milliseconds using JVM's System.currentTimeMillis().
     * @return Current time in milliseconds
     */
    actual override fun currentTimeMillis(): Long = System.currentTimeMillis()
}