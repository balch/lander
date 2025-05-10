package com.balch.lander.screens.gameplay

import androidx.compose.ui.input.key.*
import com.balch.lander.core.game.ControlInputs
import com.balch.lander.core.game.models.ThrustStrength

/**
 * Processes keyboard events to determine control inputs.
 *
 * This class is responsible for handling keyboard events such as key presses
 * and releases, and mapping them to the corresponding control inputs for a game.
 * It interprets events for thrust, left rotation, and right rotation.
 */
object KeyEventProcessor {
    /**
     * Handles a keyboard event and converts it into control inputs.
     *
     * This function processes a given keyboard event, such as key presses or releases,
     * and determines the appropriate control inputs for thrusting, rotating left, or rotating right.
     * The resulting control inputs are returned as a `ControlInputs` object.
     *
     * @param event The keyboard event to handle, which contains the key type and key code.
     * @return A `ControlInputs` object that represents the current control state
     *         based on the processed key event.
     */
    fun handleEvent(
        event: KeyEvent,
        lastControlInputs: ControlInputs
    ): ControlInputs {
        var isThrustPressed: Boolean? = null
        var isRotateRightPressed: Boolean? = null
        var isRotateLeftPressed: Boolean? = null
        if (event.type == KeyEventType.Companion.KeyDown) {
            when (event.key) {
                Key.Companion.DirectionUp -> isThrustPressed = true
                Key.Companion.DirectionRight -> isRotateRightPressed = true
                Key.Companion.DirectionLeft -> isRotateLeftPressed = true
            }
        } else if (event.type == KeyEventType.Companion.KeyUp) {
            when (event.key) {
                Key.Companion.DirectionUp -> isThrustPressed = false
                Key.Companion.DirectionRight -> isRotateRightPressed = false
                Key.Companion.DirectionLeft -> isRotateLeftPressed = false
            }
        }

        return ControlInputs(
            thrustStrength =
                isThrustPressed?.let { it -> if (it) ThrustStrength.MEDIUM else ThrustStrength.OFF }
                     ?: lastControlInputs.thrustStrength,
            rotateRight = isRotateRightPressed ?: lastControlInputs.rotateRight,
            rotateLeft = isRotateLeftPressed ?: lastControlInputs.rotateLeft,
        )
    }
}