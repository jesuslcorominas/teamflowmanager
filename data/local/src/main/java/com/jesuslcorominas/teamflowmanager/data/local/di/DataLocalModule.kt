package com.jesuslcorominas.teamflowmanager.data.local.di

import androidx.room.Room
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.callback.DatabaseCallback
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerLocalDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Internal database module providing Room database and DAOs
 */
internal val databaseModule = module {
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
 * Internal data source module providing local data source implementations
 */
internal val dataSourceLocalModule = module {
    single<PlayerLocalDataSource> { PlayerLocalDataSourceImpl(get()) }
}

/**
 * Public module that exposes data:local dependencies
 */
val dataLocalModule = module {
    includes(databaseModule, dataSourceLocalModule)
}
