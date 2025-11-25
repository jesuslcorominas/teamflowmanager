package com.jesuslcorominas.teamflowmanager.shared

/**
 * Platform interface for platform-specific implementations.
 * Each platform (Android, iOS) provides its own implementation.
 */
expect fun getPlatform(): Platform

/**
 * Platform information interface.
 */
interface Platform {
    val name: String
}

/**
 * Greeting class that uses platform-specific information.
 */
class Greeting {
    fun greet(): String {
        return "Hello from ${getPlatform().name}!"
    }
}
