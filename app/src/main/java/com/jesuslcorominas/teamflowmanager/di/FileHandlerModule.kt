package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.utils.FileHandler
import com.jesuslcorominas.teamflowmanager.ui.util.AndroidFileHandler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val fileHandlerModule =
    module {
        single { AndroidFileHandler(androidContext()) } bind FileHandler::class
    }
