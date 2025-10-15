package com.jesuslcorominas.teamflowmanager.usecase.di

import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSubstitutionsUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerTimeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerTimeUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerForMatchPauseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PausePlayerTimerForMatchPauseUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCaseImpl
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCaseImpl
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
        singleOf(::GetMatchByIdUseCaseImpl) bind GetMatchByIdUseCase::class
        singleOf(::GetAllMatchesUseCaseImpl) bind GetAllMatchesUseCase::class
        singleOf(::CreateMatchUseCaseImpl) bind CreateMatchUseCase::class
        singleOf(::UpdateMatchUseCaseImpl) bind UpdateMatchUseCase::class
        singleOf(::DeleteMatchUseCaseImpl) bind DeleteMatchUseCase::class
        singleOf(::StartMatchTimerUseCaseImpl) bind StartMatchTimerUseCase::class
        singleOf(::StartMatchUseCaseImpl) bind StartMatchUseCase::class
        singleOf(::PauseMatchTimerUseCaseImpl) bind PauseMatchTimerUseCase::class
        singleOf(::PauseMatchUseCaseImpl) bind PauseMatchUseCase::class
        singleOf(::ResumeMatchUseCaseImpl) bind ResumeMatchUseCase::class
        singleOf(::GetPlayerTimeUseCaseImpl) bind GetPlayerTimeUseCase::class
        singleOf(::GetAllPlayerTimesUseCaseImpl) bind GetAllPlayerTimesUseCase::class
        singleOf(::StartPlayerTimerUseCaseImpl) bind StartPlayerTimerUseCase::class
        singleOf(::PausePlayerTimerUseCaseImpl) bind PausePlayerTimerUseCase::class
        singleOf(::PausePlayerTimerForMatchPauseUseCaseImpl) bind PausePlayerTimerForMatchPauseUseCase::class
        singleOf(::FinishMatchUseCaseImpl) bind FinishMatchUseCase::class
        singleOf(::RegisterPlayerSubstitutionUseCaseImpl) bind RegisterPlayerSubstitutionUseCase::class
        singleOf(::GetMatchSubstitutionsUseCaseImpl) bind GetMatchSubstitutionsUseCase::class
        singleOf(::GetMatchSummaryUseCaseImpl) bind GetMatchSummaryUseCase::class
    }

val useCaseModule =
    module {
        includes(useCaseInternalModule)
    }
