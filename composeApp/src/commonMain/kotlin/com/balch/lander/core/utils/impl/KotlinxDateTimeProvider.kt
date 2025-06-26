package com.balch.lander.core.utils.impl

import com.balch.lander.core.utils.TimeProvider
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * An implementation of TimeProvider that uses kotlinx-datetime library.
 * This implementation provides accurate time using the platform's system clock.
 */
class KotlinxDateTimeProvider : TimeProvider {

    /**
     * Returns the current time in milliseconds since the Unix epoch.
     * Uses kotlinx-datetime's Clock.System to get the current instant.
     */
    @OptIn(ExperimentalTime::class)
    override fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}