package com.jesuslcorominas.teamflowmanager.viewmodel.di

import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.LoginViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SettingsViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.RealTimeTicker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModel {
            MainViewModel(
                hasNotificationPermissionBeenRequestedUseCase = get(),
                setNotificationPermissionRequestedUseCase = get()
            )
        }

        viewModel {
            SplashViewModel(
                getTeam = get(),
                getCurrentUser = get(),
                hasLocalDataWithoutUserId = get(),
                synchronizeTimeUseCase = get()
            )
        }

        viewModel {
            LoginViewModel(
                signInWithGoogleUseCase = get(),
                hasLocalDataWithoutUserId = get(),
                analyticsTracker = get()
            )
        }

        viewModel {
            PlayerViewModel(
                getPlayersUseCase = get(),
                addPlayerUseCase = get(),
                updatePlayerUseCase = get(),
                deletePlayerUseCase = get(),
                getCaptainPlayerUseCase = get(),
                updateScheduledMatchesCaptainUseCase = get(),
                playerRepository = get(),
                matchRepository = get(),
                analyticsTracker = get(),
                crashReporter = get()
            )
        }
        viewModel {
            PlayerWizardViewModel(
                getPlayerByIdUseCase = get(),
                addPlayerUseCase = get(),
                updatePlayerUseCase = get(),
                getCaptainPlayerUseCase = get(),
                updateScheduledMatchesCaptainUseCase = get(),
                playerRepository = get(),
                matchRepository = get(),
                analyticsTracker = get(),
                crashReporter = get(),
                savedStateHandle = get(),
            )
        }
        viewModel {
            TeamViewModel(
                getTeam = get(),
                getPlayers = get(),
                createTeam = get(),
                updateTeam = get(),
                getCaptainPlayer = get(),
                hasScheduledMatches = get(),
                playerRepository = get(),
                analyticsTracker = get(),
                savedStateHandle = get()
            )
        }
        viewModel {
            MatchViewModel(
                getMatchById = get(),
                getAllPlayerTimesUseCase = get(),
                getPlayersUseCase = get(),
                finishMatch = get(),
                pauseMatch = get(),
                resumeMatchUseCase = get(),
                startMatchTimerUseCase = get(),
                startPlayerTimerUseCase = get(),
                registerPlayerSubstitutionUseCase = get(),
                getMatchSummaryUseCase = get(),
                registerGoal = get(),
                startTimeoutUseCase = get(),
                endTimeoutUseCase = get(),
                getMatchReportData = get(),
                exportMatchReportToPdf = get(),
                synchronizeTimeUseCase = get(),
                playerTimeRepository = get(),
                preferencesRepository = get(),
                timeTicker = get(),
                analyticsTracker = get(),
                crashReporter = get(),
                savedStateHandle = get(),
                getMatchTimelineUseCase = get()
            )
        }
        viewModel {
            MatchListViewModel(
                getAllMatchesUseCase = get(),
                deleteMatchUseCase = get(),
                resumeMatchUseCase = get(),
                archiveMatchUseCase = get(),
                synchronizeTimeUseCase = get(),
                timeProvider = get(),
                analyticsTracker = get(),
                crashReporter = get()
            )
        }
        viewModel {
            ArchivedMatchesViewModel(
                getArchivedMatchesUseCase = get(),
                unarchiveMatchUseCase = get(),
                analyticsTracker = get(),
                crashReporter = get()
            )
        }
        viewModel {
            MatchCreationWizardViewModel(
                getPlayersUseCase = get(),
                getPreviousCaptainsUseCase = get(),
                getDefaultCaptainUseCase = get(),
                saveDefaultCaptainUseCase = get(),
                getCaptainPlayerUseCase = get(),
                getTeamUseCase = get(),
                createMatch = get(),
                getMatchByIdUseCase = get(),
                updateMatchUseCase = get(),
                analyticsTracker = get(),
                crashReporter = get(),
                savedStateHandle = get()
            )
        }
        viewModel {
            AnalysisViewModel(
                getPlayerTimeStats = get(),
                getPlayerGoalStats = get(),
                getExportData = get(),
                getTeam = get(),
                exportToPdf = get(),
                analyticsTracker = get(),
                crashReporter = get()
            )
        }
        viewModel {
            SettingsViewModel(
                exportDatabaseUseCase = get(),
                importDatabaseUseCase = get(),
                getCurrentUserUseCase = get(),
                signOutUseCase = get(),
                analyticsTracker = get()
            )
        }

        factory { RealTimeTicker(get()) } bind TimeTicker::class
    }
