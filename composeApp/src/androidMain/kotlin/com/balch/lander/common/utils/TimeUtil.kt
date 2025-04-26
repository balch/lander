package com.balch.lander.common.utils

import android.os.SystemClock

/**
 * Android implementation of TimeUtil.
 */
actual object TimeUtil {
    /**
     * Gets the current time in milliseconds using Android's SystemClock.
     * @return Current time in milliseconds
     */
    actual fun currentTimeMillis(): Long = SystemClock.elapsedRealtime()
}