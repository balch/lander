package com.balch.lander

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

actual class Platform actual constructor() {
    actual val name: String
        get() = "${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}"

    actual val context: Any
        get() = NSBundle.mainBundle
}
