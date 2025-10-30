package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.utils.PdfExporter
import com.jesuslcorominas.teamflowmanager.ui.util.PdfExporterImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val appUseCaseModule = module {
    single { PdfExporterImpl(androidContext()) } bind PdfExporter::class
}
