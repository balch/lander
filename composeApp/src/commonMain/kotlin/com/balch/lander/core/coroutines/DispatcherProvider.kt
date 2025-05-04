package com.balch.lander.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Interface for providing coroutine dispatchers.
 * This allows for dependency injection of dispatchers, making code more testable.
 */
interface DispatcherProvider {
    /**
     * Dispatcher for CPU-intensive operations.
     */
    val default: CoroutineDispatcher

    /**
     * Dispatcher for IO operations.
     */
    val io: CoroutineDispatcher

    /**
     * Dispatcher for UI operations.
     */
    val main: CoroutineDispatcher

    /**
     * Dispatcher for unconfined operations.
     */
    val unconfined: CoroutineDispatcher
}

/**
 * Default implementation of [DispatcherProvider] that uses the standard Dispatchers.
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val default: CoroutineDispatcher = Dispatchers.Default
    // In KMP, we use Default for IO operations as IO is not available in common code
    override val io: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
