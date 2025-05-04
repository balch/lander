package com.balch.lander.core.coroutines

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope

/**
 * Test implementation of [CoroutineScopeProvider] that uses a [TestScope] for testing.
 * This allows for controlled testing of coroutines.
 */
class TestCoroutineScopeProvider() : CoroutineScopeProvider {

    lateinit var scope: CoroutineScope

    override fun get(viewModel: ViewModel): CoroutineScope = scope
}