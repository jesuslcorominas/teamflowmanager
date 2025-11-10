package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.utils.MatchReportPdfExporter
import com.jesuslcorominas.teamflowmanager.domain.utils.PdfExporter
import com.jesuslcorominas.teamflowmanager.ui.util.MatchReportPdfExporterImpl
import com.jesuslcorominas.teamflowmanager.ui.util.PdfExporterImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val pdfExporterModule = module {
    single { PdfExporterImpl(get()) } bind PdfExporter::class
    single { MatchReportPdfExporterImpl(get()) } bind MatchReportPdfExporter::class
}
