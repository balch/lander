package com.balch.lander

import com.balch.lander.core.coroutines.CoroutineScopeProvider
import com.balch.lander.core.coroutines.DefaultDispatcherProvider
import com.balch.lander.core.coroutines.DispatcherProvider
import com.balch.lander.core.coroutines.ViewModelScopeProvider
import com.balch.lander.core.game.TerrainGenerator
import com.balch.lander.core.game.TerrainGeneratorImpl
import com.balch.lander.core.utils.TimeProvider
import com.balch.lander.screens.gameplay.GamePlayViewModel
import com.balch.lander.screens.startscreen.StartViewModel
import org.koin.dsl.module

/**
 * Koin module for dependency injection.
 */
val appModule = module {
    // Core
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<CoroutineScopeProvider> { ViewModelScopeProvider() }
    single { TimeProvider() }
    single<TerrainGenerator> { TerrainGeneratorImpl(get()) }

    // ViewModels
    factory { StartViewModel(get(), get()) }
    factory { GamePlayViewModel(get(), get(), get(), get()) }

}
