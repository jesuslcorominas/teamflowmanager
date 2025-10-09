package com.jesuslcorominas.teamflowmanager.usecase.di

import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCaseImpl
import org.koin.dsl.module

/**
 * Internal use case module providing use case implementations
 */
internal val useCaseInternalModule = module {
    single<GetPlayersUseCase> { GetPlayersUseCaseImpl(get()) }
}

/**
 * Public module that exposes usecase dependencies
 */
val useCaseModule = module {
    includes(useCaseInternalModule)
}
