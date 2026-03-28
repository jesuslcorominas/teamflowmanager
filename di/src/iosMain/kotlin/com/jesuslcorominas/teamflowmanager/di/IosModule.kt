package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import com.jesuslcorominas.teamflowmanager.domain.utils.MatchReportPdfExporter
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import com.jesuslcorominas.teamflowmanager.viewmodel.AcceptTeamInvitationViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubMembersViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubSettingsViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.CreateClubViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.JoinClubViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.LoginViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamDetailViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SettingsViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.createTimeTicker
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.KoinApplication
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.posix.time

/**
 * iOS-specific Koin module that provides:
 * - No-op AnalyticsTracker and CrashReporter (Firebase Analytics/Crashlytics for iOS is KMP-17+)
 * - iOS TimeProvider backed by POSIX time()
 * - Factory registrations for all ViewModels (no viewModel {} DSL on iOS — uses factory {})
 */
val iosModule =
    module {
        single<AnalyticsTracker> { NoOpAnalyticsTracker() }
        single<CrashReporter> { NoOpCrashReporter() }
        single<TimeProvider> { IosTimeProvider() }
        single<MatchReportPdfExporter> { IosMatchReportPdfExporterImpl() }
        factory { createTimeTicker(get()) } bind TimeTicker::class

        // ── ViewModels (no-param) ────────────────────────────────────────────────

        factory {
            SplashViewModel(
                getTeam = get(),
                getCurrentUser = get(),
                getUserClubMembership = get(),
                synchronizeTimeUseCase = get(),
                syncFcmTokenUseCase = get(),
                isNotificationPermissionGranted = get(),
                signOutUseCase = get(),
            )
        }
        factory {
            LoginViewModel(
                signInWithGoogleUseCase = get(),
                syncFcmTokenUseCase = get(),
                isNotificationPermissionGranted = get(),
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
        factory {
            MainViewModel(
                hasNotificationPermissionBeenRequestedUseCase = get(),
                setNotificationPermissionRequestedUseCase = get(),
                getUserClubMembership = get(),
            )
        }
        factory {
            CreateClubViewModel(
                createClubUseCase = get(),
                getCurrentUser = get(),
                syncFcmTokenUseCase = get(),
                isNotificationPermissionGranted = get(),
                analyticsTracker = get(),
            )
        }
        factory {
            JoinClubViewModel(
                joinClubByCodeUseCase = get(),
                getCurrentUser = get(),
                syncFcmTokenUseCase = get(),
                isNotificationPermissionGranted = get(),
                analyticsTracker = get(),
            )
        }
        factory {
            PlayerViewModel(
                getPlayersUseCase = get(),
                addPlayerUseCase = get(),
                updatePlayerUseCase = get(),
                deletePlayerUseCase = get(),
                getCaptainPlayerUseCase = get(),
                updateScheduledMatchesCaptainUseCase = get(),
                setPlayerAsCaptainUseCase = get(),
                removePlayerAsCaptainUseCase = get(),
                getScheduledMatchesUseCase = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            )
        }
        factory {
            TeamListViewModel(
                getTeamsByClub = get(),
                getUserClubMembership = get(),
                generateTeamInvitation = get(),
                selfAssignAsCoach = get(),
            )
        }
        factory {
            ClubMembersViewModel(
                getClubMembers = get(),
                getUserClubMembership = get(),
            )
        }
        factory {
            ClubSettingsViewModel(
                getUserClubMembership = get(),
                getClubByFirestoreId = get(),
                updateClubUseCase = get(),
                regenerateInvitationCodeUseCase = get(),
            )
        }
        factory {
            ArchivedMatchesViewModel(
                getArchivedMatchesUseCase = get(),
                unarchiveMatchUseCase = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            )
        }
        factory {
            AnalysisViewModel(
                getPlayerTimeStats = get(),
                getPlayerGoalStats = get(),
                getExportData = get(),
                getTeam = get(),
                exportToPdf = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            )
        }
        factory {
            SettingsViewModel(
                getCurrentUserUseCase = get(),
                signOutUseCase = get(),
                deleteFcmTokenUseCase = get(),
                analyticsTracker = get(),
            )
        }

        // ── ViewModels (parameterized — caller passes params via parametersOf()) ─

        factory { params ->
            PlayerWizardViewModel(
                playerId = params.get(),
                getPlayerByIdUseCase = get(),
                addPlayerUseCase = get(),
                updatePlayerUseCase = get(),
                getCaptainPlayerUseCase = get(),
                updateScheduledMatchesCaptainUseCase = get(),
                setPlayerAsCaptainUseCase = get(),
                removePlayerAsCaptainUseCase = get(),
                getScheduledMatchesUseCase = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            )
        }
        factory { params ->
            TeamViewModel(
                mode = params.get(),
                getTeam = get(),
                getPlayers = get(),
                createTeam = get(),
                updateTeam = get(),
                getCaptainPlayer = get(),
                hasScheduledMatches = get(),
                setPlayerAsCaptainUseCase = get(),
                removePlayerAsCaptainUseCase = get(),
                getUserClubMembership = get(),
                analyticsTracker = get(),
            )
        }
        factory { params ->
            MatchViewModel(
                matchId = params.get(),
                getMatchById = get(),
                getAllPlayerTimesUseCase = get(),
                getPlayersUseCase = get(),
                finishMatch = get(),
                pauseMatch = get(),
                resumeMatchUseCase = get(),
                startMatchTimerUseCase = get(),
                registerPlayerSubstitutionUseCase = get(),
                getMatchSummaryUseCase = get(),
                getMatchTimelineUseCase = get(),
                registerGoal = get(),
                startTimeoutUseCase = get(),
                endTimeoutUseCase = get(),
                getMatchReportData = get(),
                exportMatchReportToPdf = get(),
                synchronizeTimeUseCase = get(),
                startPlayerTimersBatchUseCase = get(),
                shouldShowInvalidSubstitutionAlertUseCase = get(),
                setShouldShowInvalidSubstitutionAlertUseCase = get(),
                timeTicker = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            )
        }
        factory { params ->
            MatchCreationWizardViewModel(
                matchId = params.get(),
                getPlayersUseCase = get(),
                getPreviousCaptainsUseCase = get(),
                getDefaultCaptainUseCase = get(),
                saveDefaultCaptainUseCase = get(),
                getCaptainPlayerUseCase = get(),
                getTeamUseCase = get(),
                getClubByFirestoreIdUseCase = get(),
                createMatch = get(),
                getMatchByIdUseCase = get(),
                updateMatchUseCase = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            )
        }
        factory { params ->
            PresidentTeamDetailViewModel(
                teamId = params.get(),
                getTeamById = get(),
                getPlayersByTeam = get(),
                getMatchesByTeam = get(),
            )
        }

        factory { params ->
            AcceptTeamInvitationViewModel(
                teamId = params.get(),
                acceptTeamInvitation = get(),
                getCurrentUser = get(),
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
    override fun logEvent(
        eventName: String,
        params: Map<String, Any>,
    ) = Unit

    override fun logScreenView(
        screenName: String,
        screenClass: String?,
    ) = Unit

    override fun setUserId(userId: String?) = Unit

    override fun setUserProperty(
        key: String,
        value: String?,
    ) = Unit
}

private class NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) = Unit

    override fun log(message: String) = Unit

    override fun setCustomKey(
        key: String,
        value: String,
    ) = Unit

    override fun setCustomKey(
        key: String,
        value: Int,
    ) = Unit

    override fun setCustomKey(
        key: String,
        value: Boolean,
    ) = Unit
}

private class NoOpMatchReportPdfExporter : MatchReportPdfExporter {
    override fun exportMatchReportToPdf(matchReportData: MatchReportData): String? = null
}
