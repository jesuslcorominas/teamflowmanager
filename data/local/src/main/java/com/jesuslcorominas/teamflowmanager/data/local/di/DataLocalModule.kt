package com.jesuslcorominas.teamflowmanager.data.local.di

import androidx.room.Room
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesDataSource
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_2_3
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_3_4
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_4_5
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.converters.Converters
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.RoomTransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.RoomTransactionRunner
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.TransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.datasource.GoalLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerSubstitutionLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeHistoryLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PreferencesLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.exporter.DatabaseExporterImpl
import com.jesuslcorominas.teamflowmanager.data.local.exporter.DatabaseImporterImpl
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseImporter
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
        single<Converters> {
            Converters(get())
        }

        single {
            val converters: Converters = get()

            Room
                .databaseBuilder(
                    androidContext(),
                    TeamFlowManagerDatabase::class.java,
                    "teamflowmanager_database",
                )
                .addTypeConverter(converters)
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
        }

        single { get<TeamFlowManagerDatabase>().playerDao() }
        single { get<TeamFlowManagerDatabase>().teamDao() }
        single { get<TeamFlowManagerDatabase>().matchDao() }
        single { get<TeamFlowManagerDatabase>().playerTimeDao() }
        single { get<TeamFlowManagerDatabase>().playerTimeHistoryDao() }
        single { get<TeamFlowManagerDatabase>().playerSubstitutionDao() }
        single { get<TeamFlowManagerDatabase>().goalDao() }

        singleOf(::RoomTransactionRunner) bind TransactionRunner::class
        singleOf(::RoomTransactionExecutor) bind TransactionExecutor::class
    }

internal val dataSourceLocalModule =
    module {
        // Note: MatchDataSource is now provided by data:remote module (MatchFirestoreDataSourceImpl)
        singleOf(::PlayerTimeLocalDataSourceImpl) bind PlayerTimeDataSource::class
        singleOf(::PlayerTimeHistoryLocalDataSourceImpl) bind PlayerTimeHistoryDataSource::class
        singleOf(::PlayerSubstitutionLocalDataSourceImpl) bind PlayerSubstitutionDataSource::class
        singleOf(::PreferencesLocalDataSourceImpl) bind PreferencesDataSource::class
        singleOf(::GoalLocalDataSourceImpl) bind GoalDataSource::class
    }

internal val databaseExporterModule =
    module {
        singleOf(::DatabaseExporterImpl) bind DatabaseExporter::class
        singleOf(::DatabaseImporterImpl) bind DatabaseImporter::class
    }

val dataLocalModule =
    module {
        includes(databaseModule, dataSourceLocalModule, databaseExporterModule)
    }
