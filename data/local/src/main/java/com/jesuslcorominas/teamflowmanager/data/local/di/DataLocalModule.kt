package com.jesuslcorominas.teamflowmanager.data.local.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerSubstitutionLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.BuildConfig
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.converters.Converters
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.RoomTransactionExecutor
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction.RoomTransactionRunner
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
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            if (BuildConfig.DEBUG) {
                                initDatabase(db)
                            }
                        }
                    }
                )
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

private fun initDatabase(db: SupportSQLiteDatabase) {
    db.execSQL("INSERT INTO team (id, name, coachName, delegateName) VALUES (1, 'Loyola D', 'Rubén', 'Oliver');\n")
    db.execSQL(
        "INSERT INTO players (id, firstName, lastName, number, positions, teamId, isCaptain, imageUri) VALUES\n" +
            "(1, 'Adrián', 'López Díaz', 2, 'defender,center_back', 1, 0, NULL),\n" +
            "(2, 'Daniel', 'Menéndez Iglesias', 6, 'right_back', 1, 0, NULL),\n" +
            "(3, 'Valeria', 'García García', 26, 'forward', 1, 0, NULL),\n" +
            "(4, 'Álvaro', 'Rodríguez', 7, 'forward', 1, 1, NULL),\n" +
            "(5, 'Alejandra', '-', 1, 'goalkeeper', 1, 0, NULL),\n" +
            "(6, 'Paz', '-', 15, 'midfielder', 1, 0, NULL),\n" +
            "(7, 'Martín', '-', 52, 'attacking_midfielder,defender', 1, 0, NULL),\n" +
            "(8, 'Sira', '-', 32, 'striker', 1, 0, NULL),\n" +
            "(9, 'Anuel', ',', 22, 'left_back,forward', 1, 0, NULL),\n" +
            "(10, 'Briana', ',', 12, 'left_back', 1, 0, NULL)"
    )

    db.execSQL("INSERT INTO \"match\" VALUES(1,1,'Loyola D','EFRO','Colegio Loyola',1760781600000,2,'5,1,6,2,4,10,9,3,8,7',1,'5,1,2,4,8',3140655,NULL,'FINISHED',0,2,1,6,7,0,'[{\"periodNumber\":1,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0},{\"periodNumber\":2,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0}]',2)")
    db.execSQL("INSERT INTO \"match\" VALUES(3,1,'Loyola D','Loyola C','Colegio Loyola',1761382800000,2,'5,1,6,2,4,10,9,3,8,7',1,'5,1,2,4,8',3140655,NULL,'FINISHED',0,2,1,1,15,0,'[{\"periodNumber\":1,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0},{\"periodNumber\":2,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0}]',2)")
    db.execSQL("INSERT INTO \"match\" VALUES(4,1,'Loyola D','Fozaneldi B','Las Campas',1761994800000,2,'5,1,6,2,4,10,9,3,8,7',1,'5,1,2,4,8',3140655,NULL,'FINISHED',1,2,1,5,8,0,'[{\"periodNumber\":1,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0},{\"periodNumber\":2,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0}]',2)")
    db.execSQL("INSERT INTO \"match\" VALUES(5,1,'Loyola D','Juvencia A','Colegio Loyola',1761994800000,2,'5,1,6,2,4,10,9,3,8,7',1,'5,1,2,4,8',3140655,NULL,'FINISHED',1,2,1,3,14,0,'[{\"periodNumber\":1,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0},{\"periodNumber\":2,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0}]',2)")
    db.execSQL("INSERT INTO \"match\" VALUES(8,1,'Loyola D','Escuela de Fútbol Real Oviedo','Colegio Loyola',1761390000000,2,'5,1,2,4,10,6,9,3,8,7',4,'5,1,2,4,3',0,NULL,'SCHEDULED',0,1,0,0,0,0,'[{\"periodNumber\":1,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0},{\"periodNumber\":2,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0}]',2)")
    db.execSQL("INSERT INTO \"match\" VALUES(9,1,'Loyola D','Colegio Amor de Dios','Colegio Amor de Dios',1761458400000,2,'5,1,2,4,10,6,9,3,8,7',4,'4,2,5,1,3',0,NULL,'SCHEDULED',0,1,0,0,0,0,'[{\"periodNumber\":1,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0},{\"periodNumber\":2,\"periodDuration\":1500000,\"startTimeMillis\":0,\"endTimeMillis\":0}]',2)")

    db.execSQL(
        "INSERT INTO player_time_history VALUES(1,1,1,1369195,1760778126922),\n" +
            "(2,2,1,1372675,1760778126922),\n" +
            "(3,3,1,1789681,1760778126922),\n" +
            "(4,4,1,2036847,1760778126922),\n" +
            "(5,5,1,3140655,1760778126922),\n" +
            "(6,6,1,630021,1760778126922),\n" +
            "(7,7,1,1509232,1760778126922),\n" +
            "(8,8,1,1234812,1760778126922),\n" +
            "(9,9,1,2167796,1760778126922),\n" +
            "(10,10,1,452361,1760778126922)\n"
    )
    db.execSQL(
        "INSERT INTO goal VALUES(1,1,9,1760776838452,1852185,0), " +
            "(2,1,4,1760776959713,1973446,0)," +
            "(3,1,4,1760776359713,1973446,0)," +
            "(4,1,1,1760776459713,1973446,0)," +
            "(5,1,9,1760776559713,1973446,0)," +
            "(6,1,9,1760776659713,1973446,0);"
    )
    db.execSQL(
        "INSERT INTO player_substitution VALUES(8,1,7,1,1760776921774,1935507),\n" +
            "(9,1,3,2,1760776928546,1942279),\n" +
            "(10,1,9,8,1760777524896,2538629),\n" +
            "(11,1,4,6,1760777532057,2545790),\n" +
            "(12,1,1,3,1760777651311,2665044),\n" +
            "(13,1,2,7,1760777655261,2668994),\n" +
            "(14,1,6,9,1760777851795,2865528),\n" +
            "(15,1,7,10,1760777868644,2882377);"
    )

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

internal val databaseExporterModule =
    module {
        singleOf(::DatabaseExporterImpl) bind DatabaseExporter::class
        singleOf(::DatabaseImporterImpl) bind DatabaseImporter::class
    }

val dataLocalModule =
    module {
        includes(databaseModule, dataSourceLocalModule, databaseExporterModule)
    }
