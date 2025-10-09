package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.data.core.di.dataCoreModule
import com.jesuslcorominas.teamflowmanager.data.local.di.dataLocalModule
import com.jesuslcorominas.teamflowmanager.usecase.di.useCaseModule
import com.jesuslcorominas.teamflowmanager.viewmodel.di.viewModelModule

/**
 * List of all Koin modules
 */
val appModules = listOf(
    dataLocalModule,
    dataCoreModule,
    useCaseModule,
    viewModelModule
)
