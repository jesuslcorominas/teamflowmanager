package com.jesuslcorominas.teamflowmanager.usecase.di

import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val useCaseInternalModule = module {
    singleOf(::GetPlayersUseCaseImpl) bind GetPlayersUseCase::class
}

val useCaseModule = module {
    includes(useCaseInternalModule)
}
