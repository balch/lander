package com.balch.lander.core.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * Interface for providing coroutine scopes.
 * This allows for dependency injection of coroutine scopes, making code more testable.
 */
interface CoroutineScopeProvider {
    /**
     * Returns a coroutine scope for the given context.
     * In production, this would typically return the viewModelScope for ViewModels.
     */
    operator fun get(viewModel: ViewModel): CoroutineScope
}

/**
 * Default implementation of [CoroutineScopeProvider] that uses the viewModelScope for ViewModels.
 */
class ViewModelScopeProvider : CoroutineScopeProvider {
    override fun get(viewModel: ViewModel): CoroutineScope =
        viewModel.viewModelScope
}