package com.jesuslcorominas.teamflowmanager.di

import androidx.room.Room
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.repository.PlayerRepositoryImpl
import com.jesuslcorominas.teamflowmanager.data.local.callback.DatabaseCallback
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.domain.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCaseImpl
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Database module providing Room database and DAOs
 */
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            TeamFlowManagerDatabase::class.java,
            "teamflowmanager_database"
        )
            .addCallback(DatabaseCallback())
            .build()
    }

    single { get<TeamFlowManagerDatabase>().playerDao() }
}

/**
 * Data source module providing data source implementations
 */
val dataSourceModule = module {
    single<PlayerLocalDataSource> { PlayerLocalDataSourceImpl(get()) }
}

/**
 * Repository module providing repository implementations
 */
val repositoryModule = module {
    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
}

/**
 * Use case module providing use case implementations
 */
val useCaseModule = module {
    single<GetPlayersUseCase> { GetPlayersUseCaseImpl(get()) }
}

/**
 * ViewModel module providing ViewModels
 */
val viewModelModule = module {
    viewModel { PlayerViewModel(get()) }
}

/**
 * List of all Koin modules
 */
val appModules = listOf(
    databaseModule,
    dataSourceModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
