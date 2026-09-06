package com.jesuslcorominas.teamflowmanager.domain.config

/**
 * Remotely controlled feature switches.
 *
 * Implementations read from a remote source (Firebase Remote Config on Android) with a local
 * default, so a feature can be turned on or off without shipping a new build. Values are read
 * synchronously from the last activated snapshot: a change published remotely reaches the app on
 * the next fetch, not instantly.
 */
interface FeatureFlags {
    /**
     * Whether the player photo can be added or replaced from the app.
     *
     * Off by default: the feature is built but not in use yet. Existing photos are still displayed
     * when the flag is off — only the capture/pick entry points are hidden.
     */
    val isPlayerImageUploadEnabled: Boolean
}
