package com.jesuslcorominas.teamflowmanager.di.modules

import com.jesuslcorominas.teamflowmanager.data.core.repository.MatchRepositoryImpl
import com.jesuslcorominas.teamflowmanager.domain.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.match.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.match.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.match.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.match.MatchDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val matchModule = module {
    // Repository
    single<MatchRepository> { MatchRepositoryImpl() }

    // Use Cases
    factory { GetMatchUseCase(get()) }
    factory { PauseMatchUseCase(get()) }
    factory { ResumeMatchUseCase(get()) }

    // ViewModel
    viewModel { MatchDetailViewModel(get(), get(), get()) }
}
