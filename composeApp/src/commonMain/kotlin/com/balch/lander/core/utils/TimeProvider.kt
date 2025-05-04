package com.balch.lander.core.utils

/**
 * An interface that provides time-related functionalities.
 * It serves as an abstraction for retrieving the current time and
 * checking if the reported time is accurate.
 */
interface TimeProvider {

    fun currentTimeMillis(): Long

    val isTimeAccurate: Boolean
}