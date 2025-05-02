package com.balch.lander

import com.balch.lander.core.game.TerrainGenerator
import com.balch.lander.screens.gamescreen.GameViewModel
import com.balch.lander.screens.startscreen.StartScreenViewModel
import org.koin.dsl.module

/**
 * Koin module for dependency injection.
 */
val appModule = module {
    // Models
    single { TerrainGenerator() }
    
    // ViewModels
    factory { StartScreenViewModel() }
    factory { GameViewModel(get()) }
}