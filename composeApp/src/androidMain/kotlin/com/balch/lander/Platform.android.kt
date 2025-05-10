package com.balch.lander

import android.os.Build

actual class Platform actual constructor() {
    actual val name: String
        get() = "Android ${Build.VERSION.SDK_INT}"

    actual val context: Any
        get() = AndroidContextHolder.getAppContext()
}

