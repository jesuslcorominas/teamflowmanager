package com.jesuslcorominas.teamflowmanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jesuslcorominas.teamflowmanager.data.local.dao.GoalDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.MatchDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerSubstitutionDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerTimeDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerTimeHistoryDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.TeamDao
import com.jesuslcorominas.teamflowmanager.data.local.database.utils.converters.Converters
import com.jesuslcorominas.teamflowmanager.data.local.entity.GoalEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.MatchEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerSubstitutionEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeHistoryEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.TeamEntity

@Database(
    entities = [PlayerEntity::class, TeamEntity::class, MatchEntity::class, PlayerTimeEntity::class, PlayerTimeHistoryEntity::class, PlayerSubstitutionEntity::class, GoalEntity::class],
    version = 6,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TeamFlowManagerDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    abstract fun teamDao(): TeamDao

    abstract fun matchDao(): MatchDao

    abstract fun playerTimeDao(): PlayerTimeDao

    abstract fun playerTimeHistoryDao(): PlayerTimeHistoryDao

    abstract fun playerSubstitutionDao(): PlayerSubstitutionDao

    abstract fun goalDao(): GoalDao
}
