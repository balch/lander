package com.balch.lander.core.game

import com.balch.lander.GameConfig
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.screens.gamescreen.GameStatus
import com.balch.lander.screens.gamescreen.LanderState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Physics engine for the Lunar Lander game.
 * Handles simulation of gravity, thrust, and movement of the lander.
 */
class PhysicsEngine(
    config: GameConfig
) {
    // Base gravity acceleration on the Moon (1.62 m/s²)
    // Scaled for game units
    private val baseGravity = 1.0f
    private val baseThrust = 3.0f


    // Gravity adjusted by the config
    private val gravity = baseGravity * config.gravity.value

    // Base thrust force

    // Rotation speed in degrees per second
    private val rotationSpeed = 12f

    // Fuel consumption rate per second when thrusting
    private val fuelConsumptionRate = 5f

    /**
     * Updates the game state based on physics calculations.
     * 
     * @param landerState Current state of the lander
     * @param deltaTimeMs Time elapsed since last update in milliseconds
     * @param controls Current control inputs
     * @param terrain Current terrain configuration
     * @return Updated lander state
     */
    fun update(
        landerState: LanderState,
        deltaTimeMs: Long,
        controls: ControlInputs,
        terrain: Terrain,
        config: GameConfig,
    ): LanderState {
        // Process lander state with terrain information

        // If game is not in playing state, return the current state
        if (landerState.status != GameStatus.PLAYING) {
            return landerState
        }

        val deltaTime = deltaTimeMs / 1000f

        // Calculate new rotation based on control inputs
        val newRotation = calculateNewRotation(landerState.rotation, controls, deltaTime)

        // Calculate new fuel level based on thrust
        val thrustLevel = controls.calculateThrustLevel(landerState)
        val isRotating = (controls.rotateLeft || controls.rotateRight) && landerState.fuel > 0
        val newFuel = calculateNewFuel(landerState.fuel, thrustLevel, isRotating, deltaTime)

        // Calculate new velocity based on gravity, thrust, and current velocity
        val newVelocity = calculateNewVelocity(landerState.velocity, newRotation, thrustLevel, deltaTime)

        // Calculate new position based on velocity
        val newPosition = calculateNewPosition(landerState.position, newVelocity, deltaTime)

        // Calculate distance to ground
        val distanceToGround = calculateDistanceToGround(newPosition, terrain, config)

        val distanceToSeaLevel = calculateDistanceToSeaLevel(newPosition, terrain, config)

        // Check if lander is in danger mode
        val isDangerMode = checkDangerMode(newVelocity, distanceToGround, newFuel)

        // Create a temporary game state with updated lander state for status determination
        // This is needed because we need to check if the lander has landed or crashed
        // with the new position, velocity, and rotation values before creating the final state
        val tempLanderState = landerState.copy(
            position = newPosition,
            velocity = newVelocity,
            rotation = newRotation,
            distanceToGround = distanceToGround,
            distanceToSeaLevel = distanceToSeaLevel,
            isDangerMode = isDangerMode,
        )

        // Return a new LanderState with all updated values
        // Note: Changed from state.copy() to LanderState() constructor to make it clear
        // that we're creating a new state rather than modifying an existing one.
        // Also, we're now passing the terrain explicitly to determineGameStatus
        // instead of relying on it being part of the state.
        return LanderState(
            position = newPosition,
            velocity = newVelocity,
            rotation = newRotation,
            fuel = newFuel,
            thrustStrength = controls.thrustStrength,
            distanceToGround = distanceToGround,
            distanceToSeaLevel = distanceToSeaLevel,
            isDangerMode = isDangerMode,
            status = determineGameStatus(tempLanderState, terrain),
            initialFuel = landerState.initialFuel,
        )
    }

    private fun ControlInputs.calculateThrustLevel(landerState: LanderState): Float =
        if (landerState.fuel > 0) thrustStrength.value * baseThrust
        else 0f

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
    private fun calculateNewFuel(
        currentFuel: Float,
        thrustLevel: Float,
        isRotating: Boolean,
        deltaTime: Float
    ): Float {
        val thrustingConsumption =
            (thrustLevel / baseThrust) * fuelConsumptionRate * deltaTime

        val rotatingConsumption =
            if (isRotating) (fuelConsumptionRate/10)  * deltaTime
            else 0f

        val newFuel = currentFuel - (thrustingConsumption + rotatingConsumption)
        return if (newFuel < 0) 0f else newFuel
    }

    /**
     * Calculates the new velocity based on gravity, thrust, and current velocity.
     */
    private fun calculateNewVelocity(
        currentVelocity: Vector2D,
        rotation: Float,
        thrustLevel: Float,
        deltaTime: Float
    ): Vector2D {
        // Apply gravity
        val gravityForce = Vector2D(0f, gravity)

        // Apply thrust if thrusting and has fuel
        val thrustForce = if (thrustLevel > 0) {
            // Convert rotation to radians
            val rotationRadians = rotation * PI.toFloat() / 180f
            // Calculate thrust components based on lander's orientation
            // For 0 degrees (pointing up), thrust should be upward (negative y)
            // For 90 degrees (pointing right), thrust should be rightward (positive x)
            // For -90 degrees (pointing left), thrust should be leftward (negative x)
            val thrustX = sin(rotationRadians) * thrustLevel
            val thrustY = -cos(rotationRadians) * thrustLevel
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
    private fun calculateDistanceToGround(
        position: Vector2D,
        terrain: Terrain,
        config: GameConfig
    ): Float =
        terrain.getGroundHeight(position.x) - (position.y + config.landerSize / 2)

    /**
     * Calculates the distance from the lander to the sea level.
     */
    private fun calculateDistanceToSeaLevel(
        position: Vector2D,
        terrain: Terrain,
        config: GameConfig
    ): Float =
        (terrain.points.maxOf { it.y } + 100) - (position.y + config.landerSize / 2)

    /**
     * Checks if the lander is in danger of crashing.
     */
    private fun checkDangerMode(
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
     * 
     * @param state Current lander state
     * @param terrain Current terrain configuration
     * @return The current game status (PLAYING, LANDED, or CRASHED)
     */
    private fun determineGameStatus(state: LanderState, terrain: Terrain): GameStatus {
        return when {
            state.hasLandedSuccessfully(terrain) -> GameStatus.LANDED
            state.hasCrashed(terrain) -> GameStatus.CRASHED
            else -> GameStatus.PLAYING
        }
    }
}

/**
 * Represents the control inputs from the player.
 */
data class ControlInputs(
    var thrustStrength: ThrustStrength = ThrustStrength.OFF,
    var rotateLeft: Boolean = false,
    var rotateRight: Boolean = false
)
