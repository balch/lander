package com.balch.lander.core.game

import com.balch.lander.CameraZoomLevel
import com.balch.lander.GameConfig
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.screens.gameplay.LanderState
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraTest {

    private data class CameraTestParams(
        val testCase: String,
        val expectations: String,
        val config: GameConfig,
        val landerState: LanderState,
        val expectedResults: Camera
    ) {
        fun message(index: Int): String = "$index - $testCase: $expectations"
    }

    @Test
    fun `verify cameraZoomLevel`() {
        // Arrange
        val config = GameConfig()

        val testCases = listOf(
            CameraTestParams(
                testCase = "Empty Game State",
                expectations = "CameraZoomLevel.FAR",
                config = config,
                landerState = LanderState(),
                expectedResults = Camera()
            ),
            CameraTestParams(
                testCase = "Initial Game State",
                expectations = "CameraZoomLevel.FAR",
                config = config,
                landerState = LanderState(
                    position = Vector2D(x = 500f, y = 50f),
                    distanceToSeaLevel = 800f

                ),
                expectedResults = Camera()
            ),
            CameraTestParams(
                testCase = "Transition to MEDIUM zoom",
                expectations = "CameraZoomLevel.MEDIUM",
                config = config,
                landerState = LanderState(
                    position = Vector2D(x = 500f, y = 550f),
                    distanceToSeaLevel = CameraZoomLevel.FAR.distanceThreshold - 1
                ),
                expectedResults = Camera(
                    zoomLevel = CameraZoomLevel.MEDIUM,
                    offset = Vector2D(0f, 125f)
                )
            ),
            CameraTestParams(
                testCase = "Transition to NEAR zoom",
                expectations = "CameraZoomLevel.NEAR",
                config = config,
                landerState = LanderState(
                    position = Vector2D(x = 500f, y = 750f),
                    distanceToSeaLevel = CameraZoomLevel.MEDIUM.distanceThreshold - 1
                ),
                expectedResults = Camera(
                    zoomLevel = CameraZoomLevel.CLOSE,
                    offset = Vector2D(0f, 200f)
                )
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
        val config = GameConfig()

        val scaledWidth = config.screenWidth * ( 1f - CameraZoomLevel.MEDIUM.screenOffsetMultiplier)
        val leftBorder = scaledWidth * .2f

        val testCase =
            CameraTestParams(
                testCase = "Transition to MEDIUM zoom",
                expectations = "CameraZoomLevel.MEDIUM",
                config = config,
                landerState = LanderState(
                    position = Vector2D(x = leftBorder - 1f, y = 500f),
                    distanceToSeaLevel = CameraZoomLevel.FAR.distanceThreshold - 1
                ),
                expectedResults = Camera(
                    zoomLevel = CameraZoomLevel.MEDIUM,
                    offset = Vector2D(-400f, 125f)
                )
            )

        executeCameraTest(0, testCase)
    }

    private fun executeCameraTest(index: Int, test: CameraTestParams) {

        // Act
        val camera = Camera.calculateCameraInfo(test.landerState,test.config)

        // Assert
        assertEquals(test.expectedResults.offset.x, camera.offset.x, 0.01f, test.message(index))
        assertEquals(test.expectedResults.offset.y, camera.offset.y, 0.01f, test.message(index))
        assertEquals(test.expectedResults.zoomLevel, camera.zoomLevel, test.message(index))
    }
}
