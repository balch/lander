package com.balch.lander.core.game.models

/**
 * Represents a 2D vector with x and y components.
 */
data class Vector2D(val x: Float, val y: Float) {
    operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2D = Vector2D(x * scalar, y * scalar)
}