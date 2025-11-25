package com.jesuslcorominas.teamflowmanager.shared.di

import com.jesuslcorominas.teamflowmanager.shared.viewmodel.ArchivedMatchesViewModel
import com.jesuslcorominas.teamflowmanager.shared.viewmodel.MainViewModel
import com.jesuslcorominas.teamflowmanager.shared.viewmodel.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Shared ViewModel module for Koin dependency injection.
 * Contains ViewModels that can be used on both Android and iOS.
 */
val sharedViewModelModule = module {
    viewModel {
        MainViewModel(
            hasNotificationPermissionBeenRequestedUseCase = get(),
            setNotificationPermissionRequestedUseCase = get()
        )
    }

    viewModel {
        SplashViewModel(getTeam = get())
    }

    viewModel {
        ArchivedMatchesViewModel(
            getArchivedMatchesUseCase = get(),
            unarchiveMatchUseCase = get(),
            analyticsTracker = get(),
            crashReporter = get()
        )
    }
}
