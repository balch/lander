package com.balch.lander.core.utils

/**
 * JVM/Desktop implementation of TimeUtil.
 */
actual object TimeUtil {
    /**
     * Gets the current time in milliseconds using JVM's System.currentTimeMillis().
     * @return Current time in milliseconds
     */
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
}