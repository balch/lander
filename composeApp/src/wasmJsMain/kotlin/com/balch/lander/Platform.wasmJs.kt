package com.balch.lander

actual class Platform actual constructor() {
    actual val name: String
        get() = "Web with Kotlin/Wasm"

    actual val context: Any
        get() = Unit
}
