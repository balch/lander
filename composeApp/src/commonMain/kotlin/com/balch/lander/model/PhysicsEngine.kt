package com.balch.lander.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Physics engine for the Lunar Lander game.
 * Handles simulation of gravity, thrust, and movement of the lander.
 */
class PhysicsEngine(
    private val config: GameConfig
) {
    // Base gravity acceleration on the Moon (1.62 m/s²)
    // Scaled for game units
    private val baseGravity = 0.05f

    // Gravity adjusted by the config
    private val gravity = baseGravity * config.gravity.value

    // Base thrust force
    private val baseThrust = 0.1f

    // Thrust adjusted by the config
    private val thrust = baseThrust * config.thrustStrength.value

    // Rotation speed in degrees per second
    private val rotationSpeed = 3f

    // Fuel consumption rate per second when thrusting
    private val fuelConsumptionRate = 0.5f

    /**
     * Updates the game state based on physics calculations.
     * 
     * @param state Current game state
     * @param deltaTime Time elapsed since last update in seconds
     * @param controls Current control inputs
     * @return Updated game state
     */
    fun update(state: GameState, deltaTime: Float, controls: ControlInputs): GameState {
        // If game is not in playing state, return the current state
        if (state.status != GameStatus.PLAYING) {
            return state
        }

        // Calculate new rotation based on control inputs
        val newRotation = calculateNewRotation(state.rotation, controls, deltaTime)

        // Calculate new fuel level based on thrust
        val isThrusting = controls.thrust && state.fuel > 0
        val newFuel = calculateNewFuel(state.fuel, isThrusting, deltaTime)

        // Calculate new velocity based on gravity, thrust, and current velocity
        val newVelocity = calculateNewVelocity(state.velocity, newRotation, isThrusting, newFuel > 0, deltaTime)

        // Calculate new position based on velocity
        val newPosition = calculateNewPosition(state.position, newVelocity, deltaTime)

        // Calculate distance to ground
        val distanceToGround = calculateDistanceToGround(newPosition, state.terrain)

        // Check if lander is in danger mode
        val isDangerMode = checkDangerMode(newPosition, newVelocity, distanceToGround, newFuel)

        // Check if lander has landed or crashed
        val newStatus = determineGameStatus(state.copy(
            position = newPosition,
            velocity = newVelocity,
            rotation = newRotation
        ))

        return state.copy(
            position = newPosition,
            velocity = newVelocity,
            rotation = newRotation,
            fuel = newFuel,
            isThrusting = isThrusting,
            status = newStatus,
            distanceToGround = distanceToGround,
            isDangerMode = isDangerMode
        )
    }

    /**
     * Calculates the new rotation based on control inputs.
     */
    private fun calculateNewRotation(currentRotation: Float, controls: ControlInputs, deltaTime: Float): Float {
        var newRotation = currentRotation

        if (controls.rotateLeft) {
            newRotation -= rotationSpeed * deltaTime
        }

        if (controls.rotateRight) {
            newRotation += rotationSpeed * deltaTime
        }

        // Normalize rotation to -180 to 180 degrees
        while (newRotation > 180) newRotation -= 360
        while (newRotation < -180) newRotation += 360

        return newRotation
    }

    /**
     * Calculates the new fuel level based on thrust.
     */
    private fun calculateNewFuel(currentFuel: Float, isThrusting: Boolean, deltaTime: Float): Float {
        if (!isThrusting) {
            return currentFuel
        }

        val newFuel = currentFuel - fuelConsumptionRate * deltaTime
        return if (newFuel < 0) 0f else newFuel
    }

    /**
     * Calculates the new velocity based on gravity, thrust, and current velocity.
     */
    private fun calculateNewVelocity(
        currentVelocity: Vector2D,
        rotation: Float,
        isThrusting: Boolean,
        hasFuel: Boolean,
        deltaTime: Float
    ): Vector2D {
        // Apply gravity
        val gravityForce = Vector2D(0f, gravity)

        // Apply thrust if thrusting and has fuel
        val thrustForce = if (isThrusting && hasFuel) {
            val rotationRadians = rotation * PI.toFloat() / 180f
            val thrustX = -sin(rotationRadians) * thrust
            val thrustY = -cos(rotationRadians) * thrust
            Vector2D(thrustX, thrustY)
        } else {
            Vector2D(0f, 0f)
        }

        // Calculate acceleration
        val acceleration = gravityForce + thrustForce

        // Apply acceleration to velocity
        return currentVelocity + acceleration * deltaTime
    }

    /**
     * Calculates the new position based on velocity.
     */
    private fun calculateNewPosition(currentPosition: Vector2D, velocity: Vector2D, deltaTime: Float): Vector2D {
        return currentPosition + velocity * deltaTime
    }

    /**
     * Calculates the distance from the lander to the ground directly below.
     */
    private fun calculateDistanceToGround(position: Vector2D, terrain: Terrain): Float {
        val groundHeight = terrain.getGroundHeight(position.x)
        return groundHeight - position.y
    }

    /**
     * Checks if the lander is in danger of crashing.
     */
    private fun checkDangerMode(
        position: Vector2D,
        velocity: Vector2D,
        distanceToGround: Float,
        fuel: Float
    ): Boolean {
        // Danger conditions:
        // 1. Low fuel
        // 2. High velocity near the ground
        // 3. Close to the ground

        val isFuelLow = fuel < 10f
        val isVelocityHigh = velocity.y > 3f
        val isCloseToGround = distanceToGround < 50f

        return isFuelLow || (isVelocityHigh && isCloseToGround)
    }

    /**
     * Determines the game status based on the current state.
     */
    private fun determineGameStatus(state: GameState): GameStatus {
        return when {
            state.hasLandedSuccessfully() -> GameStatus.LANDED
            state.hasCrashed() -> GameStatus.CRASHED
            else -> GameStatus.PLAYING
        }
    }
}

/**
 * Represents the control inputs from the player.
 */
data class ControlInputs(
    var thrust: Boolean = false,
    var rotateLeft: Boolean = false,
    var rotateRight: Boolean = false
)
