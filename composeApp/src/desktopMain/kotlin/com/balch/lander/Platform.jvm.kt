package com.balch.lander

actual class Platform actual constructor() {
    actual val name: String
        get() = "Java ${System.getProperty("java.version")}"

    actual val context: Any
        get() = this.javaClass.classLoader
}
