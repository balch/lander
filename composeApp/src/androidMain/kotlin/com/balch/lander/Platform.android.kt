package com.balch.lander

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    // Provide the Android application context
    override val context: Any?
        get() = AndroidContextHolder.getAppContext()
}

actual fun getPlatform(): Platform = AndroidPlatform()