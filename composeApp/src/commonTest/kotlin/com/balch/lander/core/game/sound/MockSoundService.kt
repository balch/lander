package com.balch.lander.core.game.sound

import com.balch.lander.core.game.models.ThrustStrength

/**
 * Mock implementation of SoundService for testing.
 * Tracks when sound methods are called to allow verification in tests.
 */
class MockSoundService : SoundService {
    var thrustSoundPlayed: ThrustStrength? = null
    var thrustSoundStopped = false
    var landingSuccessSoundPlayed = false
    var crashSoundPlayed = false
    var disposed = false
    
    override fun playThrustSound(thrustStrength: ThrustStrength) {
        thrustSoundPlayed = thrustStrength
    }
    
    override fun stopThrustSound() {
        thrustSoundStopped = true
    }
    
    override fun playLandingSuccessSound() {
        landingSuccessSoundPlayed = true
    }
    
    override fun playCrashSound() {
        crashSoundPlayed = true
    }
    
    override fun dispose() {
        disposed = true
    }
    
    /**
     * Resets all tracking variables to their initial state.
     * Useful for clearing state between tests.
     */
    fun reset() {
        thrustSoundPlayed = null
        thrustSoundStopped = false
        landingSuccessSoundPlayed = false
        crashSoundPlayed = false
        disposed = false
    }
}