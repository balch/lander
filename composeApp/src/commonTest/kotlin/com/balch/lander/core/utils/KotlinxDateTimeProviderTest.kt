package com.balch.lander.core.utils

import com.balch.lander.core.utils.impl.KotlinxDateTimeProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class KotlinxDateTimeProviderTest {

    @Test
    fun testCurrentTimeMillis() = runTest {
        val timeProvider = KotlinxDateTimeProvider()

        // Get the current time
        val time1 = timeProvider.currentTimeMillis()

        // The time should be a reasonable value (after year 2023)
        val year2023InMillis = 1672531200000 // 2023-01-01 00:00:00 UTC
        assertTrue(time1 > year2023InMillis, "Time should be after 2023")

        // Get two times in quick succession - they should be different or equal
        val timeA = timeProvider.currentTimeMillis()
        val timeB = timeProvider.currentTimeMillis()
        assertTrue(timeB >= timeA, "Time should not go backwards")
    }
}
