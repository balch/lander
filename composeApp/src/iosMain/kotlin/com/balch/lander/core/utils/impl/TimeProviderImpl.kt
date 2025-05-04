package com.balch.lander.core.utils.impl

import com.balch.lander.core.utils.TimeProvider
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation of TimeUtil.
 */
actual class TimeProviderImpl: TimeProvider {

    actual override val isTimeAccurate: Boolean = true
    /**
     * Gets the current time in milliseconds using iOS's NSDate.
     * @return Current time in milliseconds
     */
    actual override fun currentTimeMillis(): Long =
        (NSDate().timeIntervalSince1970() * 1000).toLong()
}