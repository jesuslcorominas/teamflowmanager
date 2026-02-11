package com.jesuslcorominas.teamflowmanager.viewmodel.di

import com.jesuslcorominas.teamflowmanager.viewmodel.AcceptTeamInvitationViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubMembersViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.CreateClubViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.JoinClubViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.LoginViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SettingsViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamListViewModel
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
                setNotificationPermissionRequestedUseCase = get(),
                getUserClubMembership = get()
            )
        }

        viewModel {
            SplashViewModel(
                getTeam = get(),
                getCurrentUser = get(),
                getUserClubMembership = get(),
                synchronizeTimeUseCase = get()
            )
        }

        viewModel {
            LoginViewModel(
                signInWithGoogleUseCase = get(),
                analyticsTracker = get()
            )
        }

        viewModel {
            CreateClubViewModel(
                createClubUseCase = get(),
                analyticsTracker = get()
            )
        }

        viewModel {
            JoinClubViewModel(
                joinClubByCodeUseCase = get(),
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
                setPlayerAsCaptainUseCase = get(),
                removePlayerAsCaptainUseCase = get(),
                getScheduledMatchesUseCase = get(),
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
                setPlayerAsCaptainUseCase = get(),
                removePlayerAsCaptainUseCase = get(),
                getScheduledMatchesUseCase = get(),
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
                setPlayerAsCaptainUseCase = get(),
                removePlayerAsCaptainUseCase = get(),
                getUserClubMembership = get(),
                analyticsTracker = get(),
                savedStateHandle = get()
            )
        }
        viewModel {
            TeamListViewModel(
                getTeamsByClub = get(),
                getUserClubMembership = get(),
                generateTeamInvitation = get(),
                selfAssignAsCoach = get()
            )
        }
        viewModel {
            ClubMembersViewModel(
                getClubMembers = get(),
                getUserClubMembership = get()
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
                savedStateHandle = get()
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
                getCurrentUserUseCase = get(),
                signOutUseCase = get(),
                analyticsTracker = get()
            )
        }

        viewModel {
            AcceptTeamInvitationViewModel(
                savedStateHandle = get(),
                acceptTeamInvitation = get(),
                getCurrentUser = get()
            )
        }

        factory { RealTimeTicker(get()) } bind TimeTicker::class
    }
