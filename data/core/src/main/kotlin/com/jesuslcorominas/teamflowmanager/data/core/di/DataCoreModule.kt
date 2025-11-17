package com.jesuslcorominas.teamflowmanager.data.core.di

import com.jesuslcorominas.teamflowmanager.data.core.repository.DatabaseRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.GoalRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.MatchRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerSubstitutionRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerTimeHistoryRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerTimeRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.PreferencesRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.TeamRepositoryImpl
import com.jesuslcorominas.teamflowmanager.usecase.repository.DatabaseRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val repositoryModule =
    module {
        singleOf(::PlayerRepositoryImpl) bind PlayerRepository::class
        singleOf(::TeamRepositoryImpl) bind TeamRepository::class
        singleOf(::MatchRepositoryImpl) bind MatchRepository::class
        singleOf(::PlayerTimeRepositoryImpl) bind PlayerTimeRepository::class
        singleOf(::PlayerTimeHistoryRepositoryImpl) bind PlayerTimeHistoryRepository::class
        singleOf(::PlayerSubstitutionRepositoryImpl) bind PlayerSubstitutionRepository::class
        singleOf(::PreferencesRepositoryImpl) bind PreferencesRepository::class
        singleOf(::GoalRepositoryImpl) bind GoalRepository::class
        singleOf(::DatabaseRepositoryImpl) bind DatabaseRepository::class
    }

val dataCoreModule =
    module {
        includes(repositoryModule)
    }
