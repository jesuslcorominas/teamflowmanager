package com.jesuslcorominas.teamflowmanager.viewmodel.di

import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDetailViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
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
            SplashViewModel(getTeam = get())
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
            )
        }
        viewModel {
            TeamViewModel(
                getTeam = get(),
                getPlayers = get(),
                createTeam = get(),
                updateTeam = get(),
                getCaptainPlayer = get(),
                playerRepository = get(),
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
                preferencesRepository = get(),
                timeTicker = get(),
                savedStateHandle = get()
            )
        }
        viewModel {
            MatchListViewModel(
                getAllMatchesUseCase = get(),
                deleteMatchUseCase = get(),
                updateMatchUseCase = get(),
                resumeMatchUseCase = get(),
                archiveMatchUseCase = get(),
            )
        }
        viewModel {
            ArchivedMatchesViewModel(
                getArchivedMatchesUseCase = get(),
                unarchiveMatchUseCase = get(),
            )
        }
        viewModel {
            MatchDetailViewModel(
                getMatchByIdUseCase = get(),
                getPlayersUseCase = get(),
            )
        }
        viewModel {
            MatchCreationWizardViewModel(
                getPlayersUseCase = get(),
                getPreviousCaptainsUseCase = get(),
                getDefaultCaptainUseCase = get(),
                saveDefaultCaptainUseCase = get(),
                getCaptainPlayerUseCase = get(),
                createMatch = get()
            )
        }
        viewModel {
            AnalysisViewModel(
                getPlayerTimeStats = get(),
                getPlayerGoalStats = get(),
                getExportData = get(),
                getTeam = get(),
                exportToPdf = get(),
            )
        }

        factory { RealTimeTicker() } bind TimeTicker::class
    }
