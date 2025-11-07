package com.jesuslcorominas.teamflowmanager.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jesuslcorominas.teamflowmanager.analytics.FirebaseAnalyticsTracker
import com.jesuslcorominas.teamflowmanager.analytics.FirebaseCrashReporter
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for analytics dependencies.
 * Provides Firebase Analytics and Crashlytics implementations.
 */
val analyticsModule =
    module {
        // Firebase instances
        single { FirebaseAnalytics.getInstance(get()) }
        single { FirebaseCrashlytics.getInstance() }

        // Analytics tracker
        singleOf(::FirebaseAnalyticsTracker) bind AnalyticsTracker::class

        // Crash reporter
        singleOf(::FirebaseCrashReporter) bind CrashReporter::class
    }
