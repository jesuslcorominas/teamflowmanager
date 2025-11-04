package com.jesuslcorominas.teamflowmanager.data.local.di

import android.content.Context
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.BuildConfig
import com.jesuslcorominas.teamflowmanager.data.local.database.DatabaseFactory
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.SqlDelightTransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.SqlDelightTransactionRunner
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.TransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.datasource.GoalLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.MatchLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerSubstitutionLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeHistoryLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PreferencesLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.TeamLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.GoalDaoWrapper
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.MatchDaoWrapper
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.PlayerDaoWrapper
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.PlayerSubstitutionDaoWrapper
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.PlayerTimeDaoWrapper
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.PlayerTimeHistoryDaoWrapper
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.TeamDaoWrapper
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val databaseModule =
    module {
        single<KotlinJsonAdapterFactory> { KotlinJsonAdapterFactory() }
        single {
            Moshi.Builder()
                .add(get<KotlinJsonAdapterFactory>())
                .build()
        }

        single {
            val context: Context = androidContext()
            val moshi: Moshi = get()
            val driver = DatabaseFactory.createDriver(context)
            
            if (BuildConfig.DEBUG) {
                DatabaseFactory.initializeDatabase(driver)
            }
            
            DatabaseFactory.createDatabase(driver, moshi)
        }

        single { PlayerDaoWrapper(get<TeamFlowManagerDatabase>()) }
        single { TeamDaoWrapper(get<TeamFlowManagerDatabase>()) }
        single { MatchDaoWrapper(get<TeamFlowManagerDatabase>()) }
        single { PlayerTimeDaoWrapper(get<TeamFlowManagerDatabase>()) }
        single { PlayerTimeHistoryDaoWrapper(get<TeamFlowManagerDatabase>()) }
        single { PlayerSubstitutionDaoWrapper(get<TeamFlowManagerDatabase>()) }
        single { GoalDaoWrapper(get<TeamFlowManagerDatabase>()) }

        singleOf(::SqlDelightTransactionRunner) bind TransactionRunner::class
        singleOf(::SqlDelightTransactionExecutor) bind TransactionExecutor::class
    }

internal val dataSourceLocalModule =
    module {
        singleOf(::PlayerLocalDataSourceImpl) bind PlayerLocalDataSource::class
        singleOf(::TeamLocalDataSourceImpl) bind TeamLocalDataSource::class
        singleOf(::MatchLocalDataSourceImpl) bind MatchLocalDataSource::class
        singleOf(::PlayerTimeLocalDataSourceImpl) bind PlayerTimeLocalDataSource::class
        singleOf(::PlayerTimeHistoryLocalDataSourceImpl) bind PlayerTimeHistoryLocalDataSource::class
        singleOf(::PlayerSubstitutionLocalDataSourceImpl) bind PlayerSubstitutionLocalDataSource::class
        singleOf(::PreferencesLocalDataSourceImpl) bind PreferencesLocalDataSource::class
        singleOf(::GoalLocalDataSourceImpl) bind GoalLocalDataSource::class
    }

val dataLocalModule =
    module {
        includes(databaseModule, dataSourceLocalModule)
    }
