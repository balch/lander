package com.balch.lander.screens.gameplay

import app.cash.turbine.test
import com.balch.lander.GameConfig
import com.balch.lander.core.coroutines.TestCoroutineScopeProvider
import com.balch.lander.core.coroutines.TestDispatcherProvider
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.TestTerrainGenerator
import com.balch.lander.core.game.models.LandingPad
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.core.game.sound.MockSoundService
import com.balch.lander.utils.TestTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GamePlayViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider = TestDispatcherProvider(testDispatcher)
    private val scopeProvider = TestCoroutineScopeProvider()

    private var terrain = Terrain(
        points = listOf(
            Vector2D(0f, 0f),
            Vector2D(100f, 0f),
            Vector2D(100f, 100f),
            Vector2D(0f, 100f)
        ),
        landingPads = listOf(
            LandingPad(
                Vector2D(2f, 2f),
                Vector2D(4f, 2f),
            ),
            LandingPad(
                Vector2D(60f, 2f),
                Vector2D(70f, 2f),
            )
        )
    )
    private val terrainGenerator by lazy { TestTerrainGenerator(terrain) }

    private val timeProvider = TestTimeProvider()
    private val mockSoundService = MockSoundService()

    private val viewModel by lazy {
        GamePlayViewModel(
            terrainGenerator = terrainGenerator,
            timeProvider = timeProvider,
            soundService = mockSoundService,
            dispatcherProvider = dispatcherProvider,
            scopeProvider = scopeProvider
        )
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        // Arrange
        scopeProvider.scope = backgroundScope

        viewModel.uiState.test {
            assertTrue(awaitItem() is GamePlayViewModel.GameScreenState.Loading)
        }
    }

    @Test
    fun `startGame transitions to Playing state`() = runTest(testDispatcher) {
        // Arrange
        scopeProvider.scope = backgroundScope
        val config = GameConfig()

        viewModel.uiState.test {
            assertTrue(awaitItem() is GamePlayViewModel.GameScreenState.Loading)

            viewModel.startGame(config)

            val item = awaitItem()
            assertTrue(item is GamePlayViewModel.GameScreenState.Playing)
            assertEquals(config, item.environmentState.config)
        }
    }

    @Test
    fun `setControlsInputs rotateLeft updates control inputs`() = runTest(testDispatcher) {
        // Arrange
        scopeProvider.scope = backgroundScope
        val config = GameConfig()

        val controlInputs = ControlInputs(
            thrustStrength = ThrustStrength.HIGH,
            rotateLeft = true,
            rotateRight = false
        )

        viewModel.uiState.test {
            viewModel.startGame(config)

            skipItems(2)

            timeProvider.advanceTimeBy()
            // Set control inputs
            viewModel.setControlsInputs(controlInputs)

            val item = awaitItem() as GamePlayViewModel.GameScreenState.Playing
            assertEquals(controlInputs.thrustStrength, item.landerState.thrustStrength)
            assertTrue { item.landerState.rotation < 0 }
        }
    }

    @Test
    fun `setControlsInputs rotateRight updates control inputs`() = runTest(testDispatcher) {
        // Arrange
        scopeProvider.scope = backgroundScope
        val config = GameConfig()

        val controlInputs = ControlInputs(
            thrustStrength = ThrustStrength.MEDIUM,
            rotateLeft = false,
            rotateRight = true
        )

        viewModel.uiState.test {
            viewModel.startGame(config)

            skipItems(2)

            timeProvider.advanceTimeBy()
            // Set control inputs
            viewModel.setControlsInputs(controlInputs)

            val item = awaitItem() as GamePlayViewModel.GameScreenState.Playing
            assertEquals(controlInputs.thrustStrength, item.landerState.thrustStrength)
            assertTrue { item.landerState.rotation > 0 }
        }
    }

    @Test
    fun `setControlsInputs plays thrust sound based on thrust strength`() = runTest(testDispatcher) {
        // Arrange
        scopeProvider.scope = backgroundScope
        mockSoundService.reset()

        viewModel.uiState.test {
            viewModel.startGame(GameConfig())

            skipItems(2)

            // Set control inputs
            timeProvider.advanceTimeBy()
            val controlInputsLow = ControlInputs(thrustStrength = ThrustStrength.LOW)
            viewModel.setControlsInputs(controlInputsLow)

            // Assert - LOW thrust sound should be played
            skipItems(1)
            assertEquals(ThrustStrength.LOW, mockSoundService.thrustSoundPlayed)

            // Act - Set control inputs with MEDIUM thrust
            timeProvider.advanceTimeBy()
            mockSoundService.reset()
            val controlInputsMedium = ControlInputs(thrustStrength = ThrustStrength.MEDIUM)
            viewModel.setControlsInputs(controlInputsMedium)

            // Assert - MEDIUM thrust sound should be played
            skipItems(1)
            assertEquals(ThrustStrength.MEDIUM, mockSoundService.thrustSoundPlayed)

            // Act - Set control inputs with OFF thrust
            timeProvider.advanceTimeBy()
            mockSoundService.reset()
            val controlInputsOff = ControlInputs(thrustStrength = ThrustStrength.OFF)
            viewModel.setControlsInputs(controlInputsOff)

            // Assert - OFF thrust should stop the sound
            skipItems(1)
            assertEquals(ThrustStrength.OFF, mockSoundService.thrustSoundPlayed)
        }
    }
}
