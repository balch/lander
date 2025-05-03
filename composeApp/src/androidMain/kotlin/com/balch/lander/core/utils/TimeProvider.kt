package com.balch.lander.core.utils

import android.os.SystemClock

/**
 * Android implementation of TimeUtil.
 */
actual class TimeProvider {

    actual val isTimeAccurate: Boolean = true

    /**
     * Gets the current time in milliseconds using Android's SystemClock.
     * @return Current time in milliseconds
     */
    actual fun currentTimeMillis(): Long = SystemClock.elapsedRealtime()
}