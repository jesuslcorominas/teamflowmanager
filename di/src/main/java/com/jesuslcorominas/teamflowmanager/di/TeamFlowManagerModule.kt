package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.data.core.di.dataCoreModule
import com.jesuslcorominas.teamflowmanager.data.local.di.dataLocalModule
// import com.jesuslcorominas.teamflowmanager.data.remote.di.dataRemoteModule // Uncomment when remote data sources are needed
import com.jesuslcorominas.teamflowmanager.usecase.di.useCaseModule
import com.jesuslcorominas.teamflowmanager.viewmodel.di.viewModelModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val teamFlowManagerModule = module {
        includes(
            listOf(
                dataLocalModule,
                dataCoreModule,
                // dataRemoteModule, // Uncomment when remote data sources are needed
                useCaseModule,
                viewModelModule,
            )
        )

        single<CoroutineDispatcher> { Dispatchers.IO }
    }
