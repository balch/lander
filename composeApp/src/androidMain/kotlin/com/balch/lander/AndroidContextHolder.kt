package com.balch.lander

import android.content.Context

// Helper object to initialize and hold the Android Application Context.
// This should be initialized from your Application class.
object AndroidContextHolder {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        // Store the application context to avoid potential memory leaks
        // associated with holding an Activity or other shorter-lived contexts.
        this.appContext = context.applicationContext
    }

    fun getAppContext(): Context {
        return appContext ?: throw IllegalStateException(
            "AndroidContextHolder has not been initialized. " +
            "Please call initialize(context) in your Android Application class's onCreate method."
        )
    }
}
