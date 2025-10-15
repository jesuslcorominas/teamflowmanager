package com.jesuslcorominas.teamflowmanager.viewmodel.di

import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDetailViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.RealTimeTicker
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.TimeTicker
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
            )
        }
        viewModel {
            TeamViewModel(
                getTeamUseCase = get(),
                createTeamUseCase = get(),
                updateTeamUseCase = get(),
            )
        }
        viewModel {
            MatchViewModel(
                getMatchUseCase = get(),
                getAllPlayerTimesUseCase = get(),
                getPlayersUseCase = get(),
                saveMatchUseCase = get(),
                pauseMatchUseCase = get(),
                resumeMatchUseCase = get(),
                registerPlayerSubstitutionUseCase = get(),
                preferencesRepository = get(),
                timeTicker = get()
            )
        }
        viewModel {
            MatchListViewModel(
                getAllMatchesUseCase = get(),
                getMatchUseCase = get(),
                deleteMatchUseCase = get(),
                createMatchUseCase = get(),
                updateMatchUseCase = get(),
                startMatchUseCase = get(),
                resumeMatchUseCase = get(),
            )
        }
        viewModel {
            MatchDetailViewModel(
                getMatchByIdUseCase = get(),
                getPlayersUseCase = get(),
            )
        }

        factory { RealTimeTicker() } bind TimeTicker::class
    }
