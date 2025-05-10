package com.balch.lander

/**
 * Represents the platform-specific environment and context.
 * Provides access to the platform name and a general context object
 * which can be used in platform-specific implementations.
 */
expect class Platform() {
    open val name: String
    open val context: Any
}

