package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.data.core.di.dataCoreModule
import com.jesuslcorominas.teamflowmanager.data.local.di.dataLocalModule
import com.jesuslcorominas.teamflowmanager.data.remote.di.dataRemoteModule
import com.jesuslcorominas.teamflowmanager.usecase.di.useCaseModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Base business-logic Koin module shared by all platforms.
 * Includes data layers + use cases + a default CoroutineDispatcher.
 */
val businessLogicModule: Module = module {
    includes(
        dataLocalModule,
        dataCoreModule,
        dataRemoteModule,
        useCaseModule,
    )
    single<CoroutineDispatcher> { Dispatchers.Default }
}

/**
 * Starts Koin with the shared business-logic module plus any platform-specific modules.
 * Called from iOS Swift code (and reusable for JVM/Desktop in the future).
 */
fun initKoin(additionalModules: List<Module> = emptyList()): KoinApplication =
    startKoin {
        modules(listOf(businessLogicModule) + additionalModules)
    }
