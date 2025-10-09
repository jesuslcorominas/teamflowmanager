package com.jesuslcorominas.teamflowmanager.data.core.di

import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerRepositoryImpl
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val repositoryModule =
    module {
        singleOf(::PlayerRepositoryImpl) bind PlayerRepository::class
    }

val dataCoreModule =
    module {
        includes(repositoryModule)
    }
