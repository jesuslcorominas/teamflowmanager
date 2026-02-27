package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import com.jesuslcorominas.teamflowmanager.viewmodel.LoginViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.KoinApplication
import org.koin.dsl.module
import platform.posix.time

/**
 * iOS-specific Koin module that provides:
 * - No-op AnalyticsTracker and CrashReporter (Firebase Analytics/Crashlytics for iOS is KMP-15+)
 * - iOS TimeProvider backed by NSDate
 * - Factory registrations for the three ViewModels needed by the iOS Phase 2 MVP
 */
val iosModule = module {
    single<AnalyticsTracker> { NoOpAnalyticsTracker() }
    single<CrashReporter> { NoOpCrashReporter() }
    single<TimeProvider> { IosTimeProvider() }

    // ViewModels registered as factory (no viewModel {} DSL on iOS)
    factory {
        SplashViewModel(
            getTeam = get(),
            getCurrentUser = get(),
            getUserClubMembership = get(),
            synchronizeTimeUseCase = get(),
        )
    }
    factory {
        LoginViewModel(
            signInWithGoogleUseCase = get(),
            analyticsTracker = get(),
        )
    }
    factory {
        MatchListViewModel(
            getAllMatchesUseCase = get(),
            deleteMatchUseCase = get(),
            resumeMatchUseCase = get(),
            archiveMatchUseCase = get(),
            synchronizeTimeUseCase = get(),
            timeProvider = get(),
            analyticsTracker = get(),
            crashReporter = get(),
        )
    }
}

/**
 * Entry point for iOS Swift code to initialise Koin.
 * Call from AppDelegate or @main struct before accessing any use cases or ViewModels.
 *
 * Swift usage:
 *   import TeamflowmanagerDi
 *   IosModuleKt.doInitKoinIos()
 */
fun initKoinIos(): KoinApplication = initKoin(additionalModules = listOf(iosModule))

// ── Private iOS stub implementations ─────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
private class IosTimeProvider : TimeProvider {
    override fun getCurrentTime(): Long = time(null).toLong() * 1000L
    override suspend fun synchronize() = Unit
    override fun getOffset(): Long = 0L
}

private class NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(eventName: String, params: Map<String, Any>) = Unit
    override fun logScreenView(screenName: String, screenClass: String?) = Unit
    override fun setUserId(userId: String?) = Unit
    override fun setUserProperty(key: String, value: String?) = Unit
}

private class NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) = Unit
    override fun log(message: String) = Unit
    override fun setCustomKey(key: String, value: String) = Unit
    override fun setCustomKey(key: String, value: Int) = Unit
    override fun setCustomKey(key: String, value: Boolean) = Unit
}
