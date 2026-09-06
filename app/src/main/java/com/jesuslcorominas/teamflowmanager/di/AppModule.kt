package com.jesuslcorominas.teamflowmanager.di

import org.koin.dsl.module

val appModule =
    module {
        includes(analyticsModule)
        includes(featureFlagsModule)
        includes(pdfExporterModule)
        includes(fileHandlerModule)
    }
