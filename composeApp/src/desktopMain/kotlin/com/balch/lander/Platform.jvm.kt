package com.balch.lander

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val context: Any
        get() = this.javaClass.classLoader
}

actual fun getPlatform(): Platform = JVMPlatform()