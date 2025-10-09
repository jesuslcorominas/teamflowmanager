package com.jesuslcorominas.teamflowmanager.viewmodel.di

import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * ViewModel module providing ViewModels
 */
val viewModelModule = module {
    viewModel { PlayerViewModel(get()) }
}
