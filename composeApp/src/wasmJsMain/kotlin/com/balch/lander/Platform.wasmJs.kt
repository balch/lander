package com.balch.lander

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val context: Any
        get() = Unit

}

actual fun getPlatform(): Platform = WasmPlatform()