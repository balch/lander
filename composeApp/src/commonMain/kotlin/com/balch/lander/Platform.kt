package com.balch.lander

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform