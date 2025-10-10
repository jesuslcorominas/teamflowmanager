package com.jesuslcorominas.teamflowmanager.viewmodel.di

import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModel {
            PlayerViewModel(
                getPlayersUseCase = get(),
                addPlayerUseCase = get(),
                deletePlayerUseCase = get(),
            )
        }
    }
