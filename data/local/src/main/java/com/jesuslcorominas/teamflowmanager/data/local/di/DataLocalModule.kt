package com.jesuslcorominas.teamflowmanager.data.local.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.datasource.MatchLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerSubstitutionLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeHistoryLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PlayerTimeLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PreferencesLocalDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.local.datasource.TeamLocalDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE match ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
    }
}

internal val databaseModule =
    module {
        single {
            Room
                .databaseBuilder(
                    androidContext(),
                    TeamFlowManagerDatabase::class.java,
                    "teamflowmanager_database",
                )
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        single { get<TeamFlowManagerDatabase>().playerDao() }
        single { get<TeamFlowManagerDatabase>().teamDao() }
        single { get<TeamFlowManagerDatabase>().matchDao() }
        single { get<TeamFlowManagerDatabase>().playerTimeDao() }
        single { get<TeamFlowManagerDatabase>().playerTimeHistoryDao() }
        single { get<TeamFlowManagerDatabase>().playerSubstitutionDao() }
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
    }

val dataLocalModule =
    module {
        includes(databaseModule, dataSourceLocalModule)
    }
