package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseImporter
import com.jesuslcorominas.teamflowmanager.ui.util.DatabaseExporterImpl
import com.jesuslcorominas.teamflowmanager.ui.util.DatabaseImporterImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseExporterModule = module {
    single { DatabaseExporterImpl(androidContext(), get()) } bind DatabaseExporter::class
    single { DatabaseImporterImpl(androidContext(), get()) } bind DatabaseImporter::class
}
