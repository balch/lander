package com.balch.lander

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val context: Any?
        get() = null

}

actual fun getPlatform(): Platform = WasmPlatform()