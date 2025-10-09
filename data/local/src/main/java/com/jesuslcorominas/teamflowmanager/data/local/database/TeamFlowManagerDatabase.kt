package com.jesuslcorominas.teamflowmanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity

@Database(entities = [PlayerEntity::class], version = 1, exportSchema = false)
abstract class TeamFlowManagerDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
}
