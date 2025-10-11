package com.jesuslcorominas.teamflowmanager.usecase.di

import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdatePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdatePlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val useCaseInternalModule =
    module {
        singleOf(::GetPlayersUseCaseImpl) bind GetPlayersUseCase::class
        singleOf(::AddPlayerUseCaseImpl) bind AddPlayerUseCase::class
        singleOf(::DeletePlayerUseCaseImpl) bind DeletePlayerUseCase::class
        singleOf(::UpdatePlayerUseCaseImpl) bind UpdatePlayerUseCase::class
        singleOf(::GetTeamUseCaseImpl) bind GetTeamUseCase::class
        singleOf(::CreateTeamUseCaseImpl) bind CreateTeamUseCase::class
        singleOf(::UpdateTeamUseCaseImpl) bind UpdateTeamUseCase::class
        singleOf(::GetMatchUseCaseImpl) bind GetMatchUseCase::class
        singleOf(::StartMatchTimerUseCaseImpl) bind StartMatchTimerUseCase::class
        singleOf(::PauseMatchTimerUseCaseImpl) bind PauseMatchTimerUseCase::class
    }

val useCaseModule =
    module {
        includes(useCaseInternalModule)
    }
