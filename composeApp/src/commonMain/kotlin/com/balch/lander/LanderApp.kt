package com.balch.lander

import androidx.compose.runtime.Composable
import com.balch.lander.di.appModule
import com.balch.lander.ui.screens.AppNavigation
import com.balch.lander.ui.theme.LanderTheme
import org.koin.compose.KoinApplication

/**
 * Main entry point for the Lunar Lander application.
 * Initializes Koin and sets up the application theme and navigation.
 */
@Composable
fun LanderApp() {
    // Initialize Koin
    KoinApplication(application = {
        modules(appModule)
    }) {
        // Set up theme and navigation
        LanderTheme {
            AppNavigation()
        }
    }
}