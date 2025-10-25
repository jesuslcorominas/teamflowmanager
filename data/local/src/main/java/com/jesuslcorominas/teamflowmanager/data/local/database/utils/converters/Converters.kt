package com.jesuslcorominas.teamflowmanager.data.local.database.utils.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.jesuslcorominas.teamflowmanager.data.local.entity.MatchPeriodEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@ProvidedTypeConverter
class Converters(moshi: Moshi) {

    private val listType = Types.newParameterizedType(List::class.java, MatchPeriodEntity::class.java)
    private val listAdapter = moshi.adapter<List<MatchPeriodEntity>>(listType)

    @TypeConverter
    fun fromMatchPeriodEntityList(value: List<MatchPeriodEntity>?): String? {
        return value?.let { listAdapter.toJson(it) }
    }

    @TypeConverter
    fun toMatchPeriodEntityList(value: String?): List<MatchPeriodEntity>? {
        return value?.let { listAdapter.fromJson(it) }
    }
}
