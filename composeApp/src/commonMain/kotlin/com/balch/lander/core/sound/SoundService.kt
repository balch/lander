package com.balch.lander.core.sound

import com.balch.lander.core.game.models.ThrustStrength

/**
 * Interface for sound services in the game.
 * Provides methods to play various game sounds.
 */
interface SoundService {
    /**
     * Plays the thrust sound for the given thrust strength.
     * If a sound is already playing, it will be stopped and the new sound will be played.
     */
    fun playThrustSound(thrustStrength: ThrustStrength)
    
    /**
     * Stops any currently playing thrust sound.
     */
    fun stopThrustSound()
    
    /**
     * Plays the landing success sound.
     */
    fun playLandingSuccessSound()
    
    /**
     * Plays the crash sound.
     */
    fun playCrashSound()
    
    /**
     * Releases all resources used by the sound service.
     */
    fun dispose()
}