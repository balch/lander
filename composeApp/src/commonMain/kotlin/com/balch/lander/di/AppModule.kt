package com.balch.lander.di

import com.balch.lander.model.GameConfig
import com.balch.lander.model.PhysicsEngine
import com.balch.lander.model.TerrainGenerator
import com.balch.lander.viewmodel.GameViewModel
import com.balch.lander.viewmodel.StartScreenViewModel
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
    
    // Physics Engine - created with a specific config
    factory { (config: GameConfig) -> PhysicsEngine(config) }
}