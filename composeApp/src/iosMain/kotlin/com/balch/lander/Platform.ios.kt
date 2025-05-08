package com.balch.lander

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    // For iOS, context can be the main bundle for resource loading
    override val context: Any
        get() = NSBundle.mainBundle
}

actual fun getPlatform(): Platform = IOSPlatform()