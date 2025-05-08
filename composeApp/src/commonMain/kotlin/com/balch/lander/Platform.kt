package com.balch.lander

interface Platform {
    val name: String
    val context: Any
}

expect fun getPlatform(): Platform