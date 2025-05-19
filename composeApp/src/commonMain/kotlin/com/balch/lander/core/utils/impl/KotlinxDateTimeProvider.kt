package com.balch.lander.core.utils.impl

import com.balch.lander.core.utils.TimeProvider
import kotlinx.datetime.Clock

/**
 * An implementation of TimeProvider that uses kotlinx-datetime library.
 * This implementation provides accurate time using the platform's system clock.
 */
class KotlinxDateTimeProvider : TimeProvider {

    /**
     * Returns the current time in milliseconds since the Unix epoch.
     * Uses kotlinx-datetime's Clock.System to get the current instant.
     */
    override fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}