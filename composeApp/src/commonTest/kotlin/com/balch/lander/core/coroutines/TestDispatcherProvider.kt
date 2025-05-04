package com.balch.lander.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * Test implementation of [DispatcherProvider] that uses a single [TestDispatcher] for all operations.
 * This allows for controlled testing of coroutines.
 */
class TestDispatcherProvider(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : DispatcherProvider {
    override val default: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val main: CoroutineDispatcher = testDispatcher
    override val unconfined: CoroutineDispatcher = testDispatcher
    
    /**
     * The test dispatcher used by this provider.
     * Exposed to allow tests to control the virtual time.
     */
    fun getTestDispatcher(): TestDispatcher = testDispatcher
}