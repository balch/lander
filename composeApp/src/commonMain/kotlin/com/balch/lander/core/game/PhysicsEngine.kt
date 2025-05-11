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
 * A class responsible for handling physics calculations and updating the game state for the lunar lander simulation.
 *
 * This class models the effects of gravity, thrust, rotation, and external inputs on the lander's behavior. It also
 * includes methods for determining collisions, flight statuses, descent conditions, and landing states.
 *
 * @property logger A logging utility for debugging and tracking events within the physics engine.
 * @property baseGravity The base gravitational acceleration applied to the lander.
 * @property baseThrust The maximum thrust force the lander can generate.
 * @property gravity The customized or dynamic gravity value affecting the lander.
 * @property rotationSpeed The rate at which the lander can rotate based on control inputs.
 * @property fuelConsumptionRate The rate at which fuel is consumed when thrust or rotation is applied.
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
     * @param config The game configuration, which includes thresholds for danger zones and landing conditions.
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
        terrain.getGroundHeight(position.x) - (position.y + config.landerOffset)

    /**
     * Calculates the distance from the lander to the sea level.
     */
    private fun calculateDistanceToSeaLevel(
        position: Vector2D,
        terrain: Terrain,
        config: GameConfig
    ): Float =
        terrain.seaLevel - (position.y + config.landerOffset)

    /**
     * Derives the flight status of the lander based on its current state and environmental conditions.
     *
     * @param config The game configuration, which includes thresholds for danger zones and landing conditions.
     * @param position The current position of the lander as a 2D vector.
     * @param velocity The current velocity of the lander as a 2D vector.
     * @param rotation The current rotation angle of the lander in degrees.
     * @param distanceToGround The vertical distance between the lander and the ground.
     * @param fuel The current amount of fuel available to the lander.
     * @param terrain The terrain data used to analyze collision and landing conditions.
     * @return The flight status, which can be one of the following:
     * - `CRASHED`: If the lander is offscreen or collides with the terrain.
     * - `WARNING` or `NOMINAL`: Determined by descent status based on velocity and remaining fuel.
     * - A status derived from the landing check if within proximity to the ground.
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
        return when {
            position.isOffscreen(config) -> FlightStatus.CRASHED
            position.hitTerrain(config, terrain) -> FlightStatus.CRASHED
            distanceToGround > dangerZoneConfig.distanceToGround ->
                deriveDescentStatus(config, velocity, fuel)
            else -> deriveLandingStatus(
                config = config,
                position = position,
                velocity = velocity,
                rotation = rotation,
                distanceToGround = distanceToGround,
                terrain = terrain
            )
        }
    }

    /**
     * Determines the descent status of the lander based on its velocity and remaining fuel.
     *
     * @param config The game configuration that includes danger zone criteria.
     * @param velocity The current velocity of the lander represented as a 2D vector.
     * @param fuel The current amount of fuel available to the lander.
     * @return A `FlightStatus` representing the descent status, which can be either `WARNING` if the
     *         lander is in the danger zone or `NOMINAL` otherwise.
     */
    private fun deriveDescentStatus(config: GameConfig, velocity: Vector2D, fuel: Float): FlightStatus {
        val dangerZoneConfig = config.dangerZoneConfig
        return if (dangerZoneConfig.isInDangerZone(fuel, velocity)) FlightStatus.WARNING
        else FlightStatus.NOMINAL
    }

    /**
     * Determines the landing status of the lander based on its position, velocity, rotation,
     * distance to the ground, and terrain configuration.
     *
     * @param config The game configuration, including safe landing parameters.
     * @param position The current position of the lander as a 2D vector.
     * @param velocity The current velocity of the lander as a 2D vector.
     * @param rotation The current rotation angle of the lander in degrees.
     * @param distanceToGround The measured vertical distance between the lander and the ground.
     * @param terrain The terrain data used for determining landing conditions.
     * @return The flight status of the lander, which can be one of the following:
     * - `LANDED`: The lander has landed safely on the surface.
     * - `ALIGNED`: The lander is aligned correctly for landing but not yet touching the ground.
     * - `CRASHED`: The lander has collided with the surface without proper alignment.
     * - `DANGER`: The lander is not in a safe position or state to land.
     */
    private fun deriveLandingStatus(
        config: GameConfig,
        position: Vector2D,
        velocity: Vector2D,
        rotation: Float,
        distanceToGround: Float,
        terrain: Terrain,
    ): FlightStatus {
        val isAligned = isLandingAligned(
            safeLandingConfig = config.safeLandingConfig,
            position = position, velocity = velocity,
            rotation = rotation,
            terrain = terrain
        )
        return when {
            isAligned && distanceToGround.isZero(config.landerSize/2) -> FlightStatus.LANDED
            isAligned -> FlightStatus.ALIGNED
            distanceToGround <= 0.0f -> FlightStatus.CRASHED
            else -> FlightStatus.DANGER
        }.also {
            logger.v { "Flight status Landing: $it isAligned=$isAligned distanceToGround=$distanceToGround" }
        }
    }

    private fun Float.isZero(tolerance: Float = 1e-7f): Boolean =
        abs(this) < tolerance

    /**
     * Determines whether the current position of the lander is colliding with the terrain.
     *
     * @param config The game configuration, which includes settings that affect terrain collision,
     *               such as the lander's size.
     * @param terrain The terrain data used to determine the ground height at specific positions.
     * @return `true` if the lander's position (considering its radius) intersects with the terrain; `false` otherwise.
     */
    private fun Vector2D.hitTerrain(config: GameConfig, terrain: Terrain): Boolean =
        (y >= terrain.getGroundHeight(x - config.landerOffset))
                || (y >= terrain.getGroundHeight(x + config.landerOffset))
            .also { hitTerrain ->
                if (hitTerrain) {
                    logger.v { "Lander Crash hitTerrain=true x=$x y=$y" }
                }
            }

    /**
     * Determines if the lander is properly aligned for a safe landing based on its position, velocity, and rotation
     * while being on a valid landing pad.
     *
     * @param safeLandingConfig Configuration containing safe landing thresholds for velocity and rotation.
     * @param position The current position of the lander represented as a 2D vector.
     * @param velocity The current velocity of the lander represented as a 2D vector.
     * @param rotation The current rotation angle of the lander in degrees.
     * @param terrain The terrain data used to verify if the lander is on a valid landing pad.
     * @return `true` if the lander is aligned for safe landing (i.e., its position is on a landing pad and its velocity
     * and rotation are within safe thresholds). Returns `false` otherwise.
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

    /**
     * Determines if the current state meets the danger zone criteria defined by the `DangerZoneConfig`.
     *
     * @param fuel The current fuel level of the lander.
     * @param velocity The current velocity of the lander represented as a 2D vector.
     * @return `true` if the lander is in the danger zone (e.g., low fuel or velocity exceeding thresholds),
     *         `false` otherwise.
     */
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

    /**
     * Determines if the current `Vector2D` position is offscreen based on the given game configuration.
     *
     * @param config The game configuration, which includes screen width and height values.
     * @return `true` if the position is offscreen (outside the bounds defined by the screen width and height), `false` otherwise.
     */
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
