package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseImporter
import com.jesuslcorominas.teamflowmanager.domain.utils.FileHandler
import com.jesuslcorominas.teamflowmanager.ui.util.AndroidFileHandler
import com.jesuslcorominas.teamflowmanager.ui.util.DatabaseExporterImpl
import com.jesuslcorominas.teamflowmanager.ui.util.DatabaseImporterImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseExporterModule = module {
    single { AndroidFileHandler(androidContext()) } bind FileHandler::class
    single { DatabaseExporterImpl(get(), get()) } bind DatabaseExporter::class
    single { DatabaseImporterImpl(get(), get()) } bind DatabaseImporter::class
}
