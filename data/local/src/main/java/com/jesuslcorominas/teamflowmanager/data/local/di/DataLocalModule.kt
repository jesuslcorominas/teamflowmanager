package com.jesuslcorominas.teamflowmanager.data.local.di

import androidx.room.Room
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerLocalDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            TeamFlowManagerDatabase::class.java,
            "teamflowmanager_database"
        )
            .build()
    }

    single { get<TeamFlowManagerDatabase>().playerDao() }
}

internal val dataSourceLocalModule = module {
    singleOf(::PlayerLocalDataSourceImpl) bind PlayerLocalDataSource::class
}

val dataLocalModule = module {
    includes(databaseModule, dataSourceLocalModule)
}
