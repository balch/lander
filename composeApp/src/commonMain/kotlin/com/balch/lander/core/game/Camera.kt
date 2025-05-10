package com.balch.lander.core.game

import com.balch.lander.CameraZoomLevel
import com.balch.lander.GameConfig
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.screens.gameplay.LanderState
import org.lighthousegames.logging.logging
import kotlin.math.abs

/**
 * Represents a camera in the game, controlling the view based on zoom level and positional offset.
 *
 * @property zoomLevel Specifies the current zoom level of the camera, determining its scale and behavior.
 * @property offset Represents the camera's positional offset in 2D space.
 */
data class Camera(
    val zoomLevel: CameraZoomLevel = CameraZoomLevel.FAR,
    val offset: Vector2D = Vector2D(0f, 0f),
) {

    companion object {
        private val logger = logging()

        /**
         * Calculates the camera scale based on the lander's distance from the ground.
         * As the lander gets closer to the ground, the camera zooms in.
         *
         * @param landerState Current state of the lander
         * @param config Game configuration
         * @return Vector2D representing the scale factor for x and y dimensions
         */
        fun calculateCameraZoomLevel(
            landerState: LanderState,
            config: GameConfig
        ): CameraZoomLevel {
            val distance = landerState.distanceToSeaLevel

            // Find the appropriate zoom level based on distance
            val zoomLevel = config.cameraConfig.zoomLevels.find {
                distance >= it.distanceThreshold
            } ?: config.cameraConfig.zoomLevels.first()

            return zoomLevel
        }

        /**
         * Calculates the camera offset based on the lander's position.
         * Adjusts the view to keep the lander in frame as it moves.
         *
         * @param landerState Current state of the lander
         * @param config Game configuration
         * @return Vector2D representing the offset in x and y dimensions (in game coordinates)
         */
        fun calculateCameraInfo(
            landerState: LanderState,
            config: GameConfig
        ): Camera {

            val zoomLevel = calculateCameraZoomLevel(
                landerState = landerState,
                config = config
            )

            // If camera is zoomed out FAR, return (0,0) offset
            if (zoomLevel == CameraZoomLevel.FAR) {
                return Camera(zoomLevel)
            }

            // Calculate horizontal offset based on lander's position
            // As lander moves toward edges, camera follows to keep it within 20% of the border
            val horizontalCenter = config.screenWidth / 2
            val borderMargin = config.screenWidth * 0.2f // 20% of screen width
            val maxHorizontalOffset = config.screenWidth * config.cameraConfig.maxHorizontalOffsetPercent

            // Calculate how far from center the lander is
            val horizontalDistanceFromCenter = landerState.position.x - horizontalCenter

            // Only start offsetting when lander is beyond the border margin
            val horizontalOffsetFactor = if (abs(horizontalDistanceFromCenter) > borderMargin) {
                // Normalize to -1..1 range, accounting for the border margin
                val adjustedDistance =
                    horizontalDistanceFromCenter - (if (horizontalDistanceFromCenter > 0) borderMargin else -borderMargin)
                val normalizedDistance = adjustedDistance / (horizontalCenter - borderMargin)
                normalizedDistance.coerceIn(-1f, 1f)
            } else {
                0f
            }

            // Calculate vertical offset based on zoom level
            val verticalOffset = config.screenWidth * zoomLevel.screenOffsetMultiplier

            val horizontalOffset = -maxHorizontalOffset * horizontalOffsetFactor
            if (horizontalOffset != 0f) {
                logger.v {
                    """
                Camera calculation:
                lander=${landerState.position}, zoomLevel=$zoomLevel, scale=${zoomLevel.scale}
                maxHorizontalOffset=$maxHorizontalOffset, horizontalOffsetFactor=$horizontalOffsetFactor
                horizontalOffset=$horizontalOffset, verticalOffset=$verticalOffset
                """.trimIndent()
                }
            }

            return Camera(
                zoomLevel = zoomLevel,
                offset = Vector2D(horizontalOffset, verticalOffset)
            )
        }
    }
}