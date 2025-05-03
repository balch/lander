package com.balch.lander.core.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation of TimeUtil.
 */
actual object TimeUtil {

    actual val isTimeAccurate: Boolean = true
    /**
     * Gets the current time in milliseconds using iOS's NSDate.
     * @return Current time in milliseconds
     */
    actual fun currentTimeMillis(): Long =
        (NSDate().timeIntervalSince1970() * 1000).toLong()
}