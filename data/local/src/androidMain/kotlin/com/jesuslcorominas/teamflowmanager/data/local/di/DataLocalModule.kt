package com.jesuslcorominas.teamflowmanager.data.local.di

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesDataSource
import com.jesuslcorominas.teamflowmanager.data.local.datasource.PreferencesLocalDataSourceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val dataLocalModule: Module = module {
    singleOf(::PreferencesLocalDataSourceImpl) bind PreferencesDataSource::class
}
