package com.jesuslcorominas.teamflowmanager.service.di

import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import com.jesuslcorominas.teamflowmanager.service.MatchNotificationControllerImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for service layer dependencies.
 * Provides match notification controller implementation.
 */
val serviceModule =
    module {
        // Match notification controller
        singleOf(::MatchNotificationControllerImpl) bind MatchNotificationController::class
    }
