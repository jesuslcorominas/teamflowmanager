package com.jesuslcorominas.teamflowmanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jesuslcorominas.teamflowmanager.data.local.dao.MatchDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerTimeDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.TeamDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.MatchEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.TeamEntity

@Database(entities = [PlayerEntity::class, TeamEntity::class, MatchEntity::class, PlayerTimeEntity::class], version = 2, exportSchema = false)
abstract class TeamFlowManagerDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    abstract fun teamDao(): TeamDao

    abstract fun matchDao(): MatchDao

    abstract fun playerTimeDao(): PlayerTimeDao
}
