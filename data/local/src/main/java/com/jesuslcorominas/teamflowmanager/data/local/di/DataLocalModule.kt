package com.jesuslcorominas.teamflowmanager.data.local.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_2_3
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_3_4
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_4_5
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.converters.Converters
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.RoomTransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.RoomTransactionRunner
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.TransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.datasource.FirebaseAuthDataSource
import com.jesuslcorominas.teamflowmanager.data.local.datasource.GoalLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.MatchLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerSubstitutionLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeHistoryLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PreferencesLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.TeamFirestoreDataSourceImpl
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
        singleOf(::PlayerLocalDataSourceImpl) bind PlayerLocalDataSource::class
        // Using Firestore for Team data instead of Room
        singleOf(::TeamFirestoreDataSourceImpl) bind TeamLocalDataSource::class
        singleOf(::MatchLocalDataSourceImpl) bind MatchLocalDataSource::class
        singleOf(::PlayerTimeLocalDataSourceImpl) bind PlayerTimeLocalDataSource::class
        singleOf(::PlayerTimeHistoryLocalDataSourceImpl) bind PlayerTimeHistoryLocalDataSource::class
        singleOf(::PlayerSubstitutionLocalDataSourceImpl) bind PlayerSubstitutionLocalDataSource::class
        singleOf(::PreferencesLocalDataSourceImpl) bind PreferencesLocalDataSource::class
        singleOf(::GoalLocalDataSourceImpl) bind GoalLocalDataSource::class
    }

internal val firebaseModule =
    module {
        single { FirebaseAuth.getInstance() }
        single { FirebaseFirestore.getInstance() }
        singleOf(::FirebaseAuthDataSource) bind AuthDataSource::class
    }

internal val databaseExporterModule =
    module {
        singleOf(::DatabaseExporterImpl) bind DatabaseExporter::class
        singleOf(::DatabaseImporterImpl) bind DatabaseImporter::class
    }

val dataLocalModule =
    module {
        includes(databaseModule, dataSourceLocalModule, databaseExporterModule, firebaseModule)
    }
