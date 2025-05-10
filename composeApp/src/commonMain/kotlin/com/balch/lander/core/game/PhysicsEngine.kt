package com.balch.lander.core.game

import com.balch.lander.DangerZoneConfig
import com.balch.lander.GameConfig
import com.balch.lander.SafeLandingConfig
import com.balch.lander.core.game.models.Terrain
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.models.Vector2D
import com.balch.lander.screens.gameplay.FlightStatus
import com.balch.lander.screens.gameplay.LanderState
import com.balch.lander.screens.gameplay.isGameOver
import org.lighthousegames.logging.logging
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Physics engine for the Lunar Lander game.
 * Handles simulation of gravity, thrust, and movement of the lander.
 */
class PhysicsEngine(
    config: GameConfig
) {
    private val logger = logging()

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
        if (landerState.isGameOver()) {
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

        val flightStatus = deriveFlightStatus(
            config = config,
            position = newPosition,
            velocity = newVelocity,
            rotation = newRotation,
            distanceToGround = distanceToGround,
            fuel = newFuel,
            terrain = terrain,
        )

        // Return a new LanderState with all updated values
        return LanderState(
            position = newPosition,
            velocity = newVelocity,
            rotation = newRotation,
            fuel = newFuel,
            thrustStrength = controls.thrustStrength,
            distanceToGround = distanceToGround,
            distanceToSeaLevel = distanceToSeaLevel,
            flightStatus = flightStatus,
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
            if (isRotating) (fuelConsumptionRate / 10) * deltaTime
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
        terrain.seaLevel - (position.y + config.landerSize / 2)

    /**
     * Checks if the lander is in danger of crashing.
     */
    private fun deriveFlightStatus(
        config: GameConfig,
        position: Vector2D,
        velocity: Vector2D,
        rotation: Float,
        distanceToGround: Float,
        fuel: Float,
        terrain: Terrain,
    ): FlightStatus {
        val dangerZoneConfig = config.dangerZoneConfig
        return if (distanceToGround > dangerZoneConfig.distanceToGround) {
            when {
                position.isOffscreen(config) -> FlightStatus.CRASHED
                dangerZoneConfig.isInDangerZone(fuel, velocity) -> FlightStatus.WARNING
                else -> FlightStatus.NOMINAL
            }.also {
                logger.v { "Flight status Approach: $it distanceToGround=$distanceToGround" }
            }
        } else {
            val isAligned = isLandingAligned(
                safeLandingConfig = config.safeLandingConfig,
                position = position, velocity = velocity,
                rotation = rotation,
                terrain = terrain
            )
            val distanceToGroundInt = distanceToGround.toInt()
            when {
                isAligned && distanceToGroundInt == 0 -> FlightStatus.LANDED
                isAligned -> FlightStatus.ALIGNED
                distanceToGroundInt <= 0 -> FlightStatus.CRASHED
                else -> FlightStatus.DANGER
            }.also {
                logger.v { "Flight status Landing: $it isAligned=$isAligned distanceToGround=$distanceToGround" }
            }
        }
    }

    /**
     * Checks if lander is aligned to land correctly
     * Conditions for successful landing:
     * 1. Lander is over a landing pad
     * 2. velocity is low
     * 3. Lander is relatively upright
     *
     * @param safeLandingConfig Configuration specifying the constraints for a safe landing,
     *                          such as velocity and rotation thresholds.
     * @param terrain The terrain to check landing pad collision against
     * @return true if the lander has landed successfully, false otherwise
     */
    private fun isLandingAligned(
        safeLandingConfig: SafeLandingConfig,
        position: Vector2D,
        velocity: Vector2D,
        rotation: Float,
        terrain: Terrain,
    ): Boolean =
        terrain.isOnLandingPad(position.x)
                && abs(velocity.x) < safeLandingConfig.velocityThreshold
                && abs(velocity.y) < safeLandingConfig.velocityThreshold
                && abs(rotation) < safeLandingConfig.rotationThreshold

    private fun DangerZoneConfig.isInDangerZone(
        fuel: Float,
        velocity: Vector2D,
    ): Boolean =
        (fuel < lowFuel
            || abs(velocity.x) > velocityThreshold
            || abs(velocity.y) > velocityThreshold)
            .also { dangerZone ->
                if (dangerZone) {
                    logger.v { "Lander In Danger Zone: fuel=$fuel velocity=$velocity" }
                }
            }

    private fun Vector2D.isOffscreen(config: GameConfig): Boolean =
        (x < 0 || y < 0 || x > config.screenWidth || y > config.screenHeight)
            .also { offscreen ->
                if (offscreen) {
                    logger.v { "Lander Crash isOffscreen=true x=$x y=$y" }
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
