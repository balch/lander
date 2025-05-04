package com.balch.lander.core.utils

interface TimeProvider {

    fun currentTimeMillis(): Long

    val isTimeAccurate: Boolean
}