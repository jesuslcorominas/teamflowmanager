package com.jesuslcorominas.teamflowmanager.data.local.di

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesDataSource
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PreferencesLocalDataSourceImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


internal val dataSourceLocalModule =
    module {
        singleOf(::PreferencesLocalDataSourceImpl) bind PreferencesDataSource::class
    }

val dataLocalModule =
    module {
        includes(dataSourceLocalModule)

        single<KotlinJsonAdapterFactory> { KotlinJsonAdapterFactory() }
        single {
            Moshi.Builder()
                .add(get<KotlinJsonAdapterFactory>())
                .build()
        }
    }
