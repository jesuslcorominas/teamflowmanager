package com.jesuslcorominas.teamflowmanager

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import com.jesuslcorominas.teamflowmanager.di.appModule
import com.jesuslcorominas.teamflowmanager.di.teamFlowManagerModule
import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import com.jesuslcorominas.teamflowmanager.service.MatchNotificationServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TeamFlowManagerApplication : Application(), ImageLoaderFactory {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var notificationServiceManager: MatchNotificationServiceManager

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TeamFlowManagerApplication)
            modules(appModule, teamFlowManagerModule)
        }

        // Synchronize time with server on app startup
        val timeProvider: TimeProvider by inject()
        applicationScope.launch(Dispatchers.IO) {
            try {
                timeProvider.synchronize()
            } catch (e: Exception) {
                // Log but don't crash - time sync will be attempted again when starting matches
                android.util.Log.w("TeamFlowManager", "Failed to synchronize time on startup", e)
            }
        }

        // Start observing active matches for notification after Koin is initialized
        val matchNotificationController: MatchNotificationController by inject()
        notificationServiceManager =
            MatchNotificationServiceManager(
                context = this,
                matchNotificationController = matchNotificationController,
                scope = applicationScope,
            )
        notificationServiceManager.start()
    }

    override fun onTerminate() {
        super.onTerminate()
        notificationServiceManager.stop()
    }

    override fun newImageLoader(): ImageLoader = ImageLoader
        .Builder(this)
        .logger(DebugLogger())
        .build()
}
