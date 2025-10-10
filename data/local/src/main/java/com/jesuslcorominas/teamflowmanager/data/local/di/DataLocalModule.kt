package com.jesuslcorominas.teamflowmanager.data.local.di

import androidx.room.Room
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.TeamLocalDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val databaseModule =
    module {
        single {
            Room
                .databaseBuilder(
                    androidContext(),
                    TeamFlowManagerDatabase::class.java,
                    "teamflowmanager_database",
                ).fallbackToDestructiveMigration()
                .build()
        }

        single { get<TeamFlowManagerDatabase>().playerDao() }
        single { get<TeamFlowManagerDatabase>().teamDao() }
    }

internal val dataSourceLocalModule =
    module {
        singleOf(::PlayerLocalDataSourceImpl) bind PlayerLocalDataSource::class
        singleOf(::TeamLocalDataSourceImpl) bind TeamLocalDataSource::class
    }

val dataLocalModule =
    module {
        includes(databaseModule, dataSourceLocalModule)
    }
