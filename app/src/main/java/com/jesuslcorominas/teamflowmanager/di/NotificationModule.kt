package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import com.jesuslcorominas.teamflowmanager.notification.MatchNotificationControllerImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for notification dependencies.
 * Provides match notification controller implementation.
 */
val notificationModule =
    module {
        // Match notification controller
        singleOf(::MatchNotificationControllerImpl) bind MatchNotificationController::class
    }
