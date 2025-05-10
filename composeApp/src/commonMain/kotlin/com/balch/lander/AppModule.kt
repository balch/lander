package com.balch.lander

import com.balch.lander.core.coroutines.CoroutineScopeProvider
import com.balch.lander.core.coroutines.DefaultDispatcherProvider
import com.balch.lander.core.coroutines.DispatcherProvider
import com.balch.lander.core.coroutines.ViewModelScopeProvider
import com.balch.lander.core.game.TerrainGenerator
import com.balch.lander.core.game.TerrainGeneratorImpl
import com.balch.lander.core.game.sound.SoundService
import com.balch.lander.core.game.sound.impl.SoundServiceImpl
import com.balch.lander.core.utils.TimeProvider
import com.balch.lander.core.utils.impl.TimeProviderImpl
import com.balch.lander.screens.gameplay.GamePlayViewModel
import com.balch.lander.screens.start.StartViewModel
import org.koin.dsl.module

/**
 * Koin module for dependency injection.
 */
val appModule = module {
    // Core
    single<Platform> { Platform() }
    single<CoroutineScopeProvider> { ViewModelScopeProvider() }
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<SoundService> { SoundServiceImpl() }
    single<TerrainGenerator> { TerrainGeneratorImpl(get()) }
    single<TimeProvider> { TimeProviderImpl() }

    // ViewModels
    factory { params ->
        GamePlayViewModel(
            terrainGenerator = get(),
            timeProvider = get(),
            soundService = get(),
            dispatcherProvider = get(),
            scopeProvider = get(),
            platform = get(),
        )
    }
    factory { StartViewModel(get(), get()) }
}
