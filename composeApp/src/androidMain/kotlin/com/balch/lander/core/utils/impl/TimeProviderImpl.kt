package com.balch.lander.core.utils.impl

import android.os.SystemClock
import com.balch.lander.core.utils.TimeProvider

/**
 * Android implementation of TimeUtil.
 */
actual class TimeProviderImpl: TimeProvider {

    actual override val isTimeAccurate: Boolean = true

    /**
     * Gets the current time in milliseconds using Android's SystemClock.
     * @return Current time in milliseconds
     */
    actual  override fun currentTimeMillis(): Long = SystemClock.elapsedRealtime()
}