package com.balch.lander.screens.gameplay

import app.cash.turbine.test
import com.balch.lander.CameraZoomLevel
import com.balch.lander.GameConfig
import com.balch.lander.core.coroutines.TestCoroutineScopeProvider
import com.balch.lander.core.coroutines.TestDispatcherProvider
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.TestTerrainGenerator
import com.balch.lander.core.game.models.LandingPad
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.utils.TestTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
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

    private val viewModel by lazy {
        GamePlayViewModel(
            terrainGenerator = terrainGenerator,
            timeProvider = timeProvider,
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

    private data class CameraTestResults(
        val cameraZoomLevel: CameraZoomLevel = CameraZoomLevel.FAR,
        val offset: Vector2D = Vector2D(0f, 0f),
    )

    private data class CameraTestParams(
        val testCase: String,
        val expectations: String,
        val config: GameConfig,
        val landerState: LanderState,
        val expectedResults: CameraTestResults
    ) {
        fun message(index: Int): String = "$index - $testCase: $expectations"
    }

    @Test
    fun `verify cameraZoomLevel`() {
        // Arrange
        val testCases = listOf(
            CameraTestParams(
                testCase = "Initial Game State",
                expectations = "CameraZoomLevel.FAR",
                config = GameConfig(),
                landerState = LanderState(),
                expectedResults = CameraTestResults()
            ),
            CameraTestParams(
                testCase = "Transition to MEDIUM zoom",
                expectations = "CameraZoomLevel.MEDIUM",
                config = GameConfig(),
                landerState = LanderState(
                    distanceToSeaLevel = CameraZoomLevel.FAR.distanceThreshold - 1
                ),
                expectedResults = CameraTestResults(CameraZoomLevel.MEDIUM)
            ),
            CameraTestParams(
                testCase = "Transition to NEAR zoom",
                expectations = "CameraZoomLevel.NEAR",
                config = GameConfig(),
                landerState = LanderState(
                    distanceToSeaLevel = CameraZoomLevel.MEDIUM.distanceThreshold - 1
                ),
                expectedResults = CameraTestResults(CameraZoomLevel.CLOSE)
            ),
        )

        testCases.forEachIndexed { index, test ->
            try {
                executeCameraTest(index, test)
                println("Test case $index - ${test.testCase} passed")
            } catch (e: Exception) {
                println("Test case $index - ${test.testCase} failed")
                throw e
            }
        }
    }

    /**
     * Test used to debug individual tests used in
     * the `verify cameraZoomLevel` test above
     */
    @Ignore
    @Test
    fun `debug cameraZoomLevel test case`() {
        // Arrange
        val testCase =
            CameraTestParams(
                testCase = "Transition to MEDIUM zoom",
                expectations = "CameraZoomLevel.MEDIUM",
                config = GameConfig(),
                landerState = LanderState(
                    distanceToSeaLevel = CameraZoomLevel.FAR.distanceThreshold + 1
                ),
                expectedResults = CameraTestResults(CameraZoomLevel.MEDIUM)
            )
        executeCameraTest(0, testCase)
    }

    private fun executeCameraTest(index: Int, test: CameraTestParams) {
        // Act
        val zoomLevel = viewModel.calculateCameraZoomLevel(test.landerState, test.config)

        // Assert
        assertEquals(test.expectedResults.cameraZoomLevel, zoomLevel, test.message(index))

        // Act
        val offset = viewModel.calculateCameraOffset(test.landerState, zoomLevel, test.config)

        // Assert
//            assertEquals(test.expectedResults.offset, offset, test.message(index))
    }
}
