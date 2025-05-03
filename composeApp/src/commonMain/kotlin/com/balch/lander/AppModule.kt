package com.balch.lander

import com.balch.lander.core.game.TerrainGenerator
import com.balch.lander.core.utils.TimeProvider
import com.balch.lander.screens.gamescreen.GameViewModel
import com.balch.lander.screens.startscreen.StartScreenViewModel
import org.koin.dsl.module

/**
 * Koin module for dependency injection.
 */
val appModule = module {
    // Models
    single { TimeProvider() }
    single { TerrainGenerator(get()) }
    
    // ViewModels
    factory { StartScreenViewModel() }
    factory { GameViewModel(get(), get()) }
}