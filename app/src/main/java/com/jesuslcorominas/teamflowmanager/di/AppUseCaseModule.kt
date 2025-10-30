package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.usecase.ExportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.usecase.impl.ExportToPdfUseCaseImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val appUseCaseModule = module {
    single { ExportToPdfUseCaseImpl(androidContext()) } bind ExportToPdfUseCase::class
}
