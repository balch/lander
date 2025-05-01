package com.balch.lander.core.game.models

/**
 * Thrust strength options available in the game.
 */
enum class ThrustStrength(val value: Float, val label: String) {
    OFF(0.0f, "Off"),
    LOW(0.5f, "Lo"),
    MEDIUM(1.0f, "Mid"),
    HIGH(2.0f, "Hi")
}
