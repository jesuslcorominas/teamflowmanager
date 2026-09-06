package com.jesuslcorominas.teamflowmanager.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jesuslcorominas.teamflowmanager.config.RemoteConfigFeatureFlags
import com.jesuslcorominas.teamflowmanager.domain.config.FeatureFlags
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for remotely controlled feature switches, backed by Firebase Remote Config.
 */
val featureFlagsModule =
    module {
        single { FirebaseRemoteConfig.getInstance() }

        singleOf(::RemoteConfigFeatureFlags) bind FeatureFlags::class
    }
