package com.jesuslcorominas.teamflowmanager.viewmodel.di

import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDetailViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchSummaryViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.RealTimeTicker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val viewModelModule =
    module {
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
                getTeamUseCase = get(),
                createTeamUseCase = get(),
                updateTeamUseCase = get(),
                getCaptainPlayerUseCase = get(),
                playerRepository = get(),
            )
        }
        viewModel {
            MatchViewModel(
                getMatchById = get(),
                getAllPlayerTimesUseCase = get(),
                getPlayersUseCase = get(),
                saveMatchUseCase = get(),
                pauseMatchUseCase = get(),
                resumeMatchUseCase = get(),
                startMatchTimerUseCase = get(),
                startPlayerTimerUseCase = get(),
                registerPlayerSubstitutionUseCase = get(),
                getMatchSummaryUseCase = get(),
                registerGoalUseCase = get(),
                getGoalsForMatchUseCase = get(),
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
            MatchSummaryViewModel(
                getMatchSummaryUseCase = get(),
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

        factory { RealTimeTicker() } bind TimeTicker::class
    }
