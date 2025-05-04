package com.balch.lander.core.coroutines

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope

/**
 * Test implementation of [CoroutineScopeProvider] that uses a [TestScope] for testing.
 * This allows for controlled testing of coroutines.
 */
class TestCoroutineScopeProvider() : CoroutineScopeProvider {

    /**
     * The test scope used by this provider.
     * Exposed to allow tests to control the virtual time.
     */
    lateinit var scope: TestScope

    override fun get(viewModel: ViewModel): CoroutineScope = scope
}