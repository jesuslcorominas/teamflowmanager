package com.jesuslcorominas.teamflowmanager.data.local.di

import androidx.room.Room
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_2_3
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_3_4
import com.jesuslcorominas.teamflowmanager.data.local.database.migration.MIGRATION_4_5
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.converters.Converters
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.RoomTransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.TransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.datasource.GoalLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.MatchLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerSubstitutionLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeHistoryLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PreferencesLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.TeamLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.exporter.DatabaseExporterImpl
import com.jesuslcorominas.teamflowmanager.data.local.exporter.DatabaseImporterImpl
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseImporter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
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

        // Keep RoomTransactionExecutor for Room-specific operations
        singleOf(::RoomTransactionExecutor) bind TransactionExecutor::class
    }

internal val dataSourceLocalModule =
    module {
        single(named("PLAYER_LOCAL_DATA_SOURCE_IMPL")) { PlayerLocalDataSourceImpl(get()) } bind PlayerDataSource::class
        single(named("TEAM_LOCAL_DATA_SOURCE_IMPL")) { TeamLocalDataSourceImpl(get()) } bind TeamDataSource::class
        single(named("MATCH_LOCAL_DATA_SOURCE_IMPL")) { MatchLocalDataSourceImpl(get()) } bind MatchDataSource::class
        single(named("PLAYER_TIME_LOCAL_DATA_SOURCE_IMPL")) { PlayerTimeLocalDataSourceImpl(get()) } bind PlayerTimeDataSource::class
        single(named("PLAYER_TIME_HISTORY_LOCAL_DATA_SOURCE_IMPL")) { PlayerTimeHistoryLocalDataSourceImpl(get()) } bind PlayerTimeHistoryDataSource::class
        single(named("PLAYER_SUBSTITUTION_LOCAL_DATA_SOURCE_IMPL")) { PlayerSubstitutionLocalDataSourceImpl(get()) } bind PlayerSubstitutionDataSource::class
        single(named("GOAL_LOCAL_DATA_SOURCE_IMPL")) { GoalLocalDataSourceImpl(get()) } bind GoalDataSource::class

        singleOf(::PreferencesLocalDataSourceImpl) bind PreferencesDataSource::class
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
