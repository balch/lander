package com.balch.lander.screens.startscreen

import app.cash.turbine.test
import com.balch.lander.GameConfig
import com.balch.lander.GravityLevel
import com.balch.lander.LandingPadSize
import com.balch.lander.core.coroutines.TestCoroutineScopeProvider
import com.balch.lander.core.coroutines.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StartScreenViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider = TestDispatcherProvider(testDispatcher)
    private val scopeProvider = TestCoroutineScopeProvider()

    private val viewModel by lazy {
        StartScreenViewModel(dispatcherProvider, scopeProvider)
    }

    @Test
    fun `updateGameConfig updates uiState with new config`() = runTest(testDispatcher) {
        // Arrange
        scopeProvider.scope = backgroundScope
        val newConfig = GameConfig(
            gravity = GravityLevel.HIGH,
            fuelLevel = 0.8f,
            landingPadSize = LandingPadSize.SMALL
        )
        
        viewModel.uiState.test {
            assertEquals(GameConfig(), awaitItem().gameConfig)

            viewModel.updateGameConfig(newConfig)
            assertEquals(newConfig, awaitItem().gameConfig)
        }
    }

    @Test
    fun `updateFuelLevel updates only fuel level in config`() = runTest(testDispatcher) {
        // Arrange
        scopeProvider.scope = backgroundScope
        val newFuelLevel = 0.75f

        viewModel.uiState.test {
            val initialConfig = awaitItem().gameConfig

            viewModel.updateFuelLevel(newFuelLevel)

            val item = awaitItem()

            assertEquals(newFuelLevel, item.gameConfig.fuelLevel)
            assertEquals(initialConfig.gravity, item.gameConfig.gravity)
            assertEquals(initialConfig.landingPadSize, item.gameConfig.landingPadSize)
        }
    }
}