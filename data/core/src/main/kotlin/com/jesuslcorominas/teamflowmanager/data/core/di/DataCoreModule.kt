package com.jesuslcorominas.teamflowmanager.data.core.di

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerRepositoryImpl
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import org.koin.dsl.module

/**
 * Internal repository module providing repository implementations
 */
internal val repositoryModule = module {
    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
}

/**
 * Public module that exposes data:core dependencies
 */
val dataCoreModule = module {
    includes(repositoryModule)
}
