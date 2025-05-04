package com.balch.lander

import com.balch.lander.core.coroutines.CoroutineScopeProvider
import com.balch.lander.core.coroutines.DefaultDispatcherProvider
import com.balch.lander.core.coroutines.DispatcherProvider
import com.balch.lander.core.coroutines.ViewModelScopeProvider
import com.balch.lander.core.game.TerrainGenerator
import com.balch.lander.core.utils.TimeProvider
import com.balch.lander.screens.gamescreen.GameViewModel
import com.balch.lander.screens.startscreen.StartScreenViewModel
import org.koin.dsl.module

/**
 * Koin module for dependency injection.
 */
val appModule = module {
    // Core
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<CoroutineScopeProvider> { ViewModelScopeProvider() }
    single { TimeProvider() }
    single { TerrainGenerator(get()) }

    // ViewModels
    factory { StartScreenViewModel(get(), get()) }
    factory { GameViewModel(get(), get(), get(), get()) }

}
