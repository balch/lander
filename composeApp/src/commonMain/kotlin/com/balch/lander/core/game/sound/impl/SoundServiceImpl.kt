package com.balch.lander.core.game.sound.impl

import app.lexilabs.basic.sound.Audio
import app.lexilabs.basic.sound.ExperimentalBasicSound
import com.balch.lander.Platform
import com.balch.lander.core.game.models.ThrustStrength
import com.balch.lander.core.game.sound.SoundService
import lander.composeapp.generated.resources.Res
import org.lighthousegames.logging.logging

/**
 * Implementation of SoundService that generates and stores sounds in memory.
 * This implementation uses the LexiLabs Basic Sound library for sound generation and playback.
 */
@OptIn(ExperimentalBasicSound::class)
class SoundServiceImpl : SoundService {
    private val logger = logging()
    private val platform = Platform()

    private val thrustSounds: List<Audio> = listOf(
        Audio(platform.context, Res.getUri("files/thrust-low.mp3")),
        Audio(platform.context, Res.getUri("files/thrust-mid.mp3")),
        Audio(platform.context, Res.getUri("files/thrust-high.mp3"))
    )

    override fun playThrustSound(thrustStrength: ThrustStrength) {
        logger.info { "Playing thrust sound for level: $thrustStrength" }
        stopThrustSound()
        when (thrustStrength) {
            ThrustStrength.OFF -> Unit
            ThrustStrength.LOW -> thrustSounds[0].play()
            ThrustStrength.MEDIUM -> thrustSounds[1].play()
            ThrustStrength.HIGH -> thrustSounds[2].play()
        }
    }

    override fun stopThrustSound() {
        thrustSounds.forEach { it.stop() }
    }

    override fun playLandingSuccessSound() {
        logger.info { "Playing landing success sound" }
    }

    override fun playCrashSound() {
        logger.info { "Playing crash sound" }
    }

    override fun dispose() {
        // Stop all sounds
        stopThrustSound()
        logger.info { "Sound service disposed" }
    }
}