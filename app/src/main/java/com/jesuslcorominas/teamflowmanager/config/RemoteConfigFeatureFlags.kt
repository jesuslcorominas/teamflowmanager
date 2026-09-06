package com.jesuslcorominas.teamflowmanager.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.jesuslcorominas.teamflowmanager.BuildConfig
import com.jesuslcorominas.teamflowmanager.domain.config.FeatureFlags

/**
 * [FeatureFlags] backed by Firebase Remote Config.
 *
 * Flags are published from the Firebase console (Remote Config → parameters) using the keys in
 * [Keys]. Until the first successful fetch — and whenever the device is offline — the values in
 * [defaults] apply, so a flag that is off by default stays off on a cold start with no network.
 *
 * The fetch is fired once on construction and its result is activated for the *next* read, which
 * is Remote Config's normal behaviour: a change published remotely reaches the app on the
 * following launch rather than instantly.
 */
class RemoteConfigFeatureFlags(
    private val remoteConfig: FirebaseRemoteConfig,
) : FeatureFlags {
    object Keys {
        const val PLAYER_IMAGE_UPLOAD_ENABLED = "player_image_upload_enabled"
    }

    init {
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(
                    if (BuildConfig.DEBUG) DEBUG_FETCH_INTERVAL_SECONDS else FETCH_INTERVAL_SECONDS,
                )
                .build(),
        )
        remoteConfig.setDefaultsAsync(defaults)
        remoteConfig.fetchAndActivate()
    }

    override val isPlayerImageUploadEnabled: Boolean
        get() = remoteConfig.getBoolean(Keys.PLAYER_IMAGE_UPLOAD_ENABLED)

    companion object {
        private const val FETCH_INTERVAL_SECONDS = 3600L
        private const val DEBUG_FETCH_INTERVAL_SECONDS = 0L

        /** Applied until the first successful fetch, and whenever the device is offline. */
        private val defaults =
            mapOf<String, Any>(
                Keys.PLAYER_IMAGE_UPLOAD_ENABLED to false,
            )
    }
}
