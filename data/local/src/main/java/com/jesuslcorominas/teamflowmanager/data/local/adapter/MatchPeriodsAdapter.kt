package com.jesuslcorominas.teamflowmanager.data.local.adapter

import app.cash.sqldelight.ColumnAdapter
import com.jesuslcorominas.teamflowmanager.data.local.entity.MatchPeriodEntity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class MatchPeriodsAdapter(moshi: Moshi) : ColumnAdapter<List<MatchPeriodEntity>, String> {

    private val listType = Types.newParameterizedType(List::class.java, MatchPeriodEntity::class.java)
    private val listAdapter: JsonAdapter<List<MatchPeriodEntity>> = moshi.adapter(listType)

    override fun decode(databaseValue: String): List<MatchPeriodEntity> {
        return listAdapter.fromJson(databaseValue) ?: emptyList()
    }

    override fun encode(value: List<MatchPeriodEntity>): String {
        return listAdapter.toJson(value)
    }
}
