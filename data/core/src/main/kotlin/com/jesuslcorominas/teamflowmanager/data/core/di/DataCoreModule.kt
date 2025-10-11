package com.jesuslcorominas.teamflowmanager.data.core.di

import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.SessionRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.core.repository.TeamRepositoryImpl
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.SessionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val repositoryModule =
    module {
        singleOf(::PlayerRepositoryImpl) bind PlayerRepository::class
        singleOf(::TeamRepositoryImpl) bind TeamRepository::class
        singleOf(::SessionRepositoryImpl) bind SessionRepository::class
    }

val dataCoreModule =
    module {
        includes(repositoryModule)
    }
