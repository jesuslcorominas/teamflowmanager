package com.jesuslcorominas.teamflowmanager

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import com.jesuslcorominas.teamflowmanager.di.appModule
import com.jesuslcorominas.teamflowmanager.di.teamFlowManagerModule
import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import com.jesuslcorominas.teamflowmanager.service.MatchNotificationServiceManager
import com.jesuslcorominas.teamflowmanager.usecase.HasLocalDataWithoutUserIdUseCase
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

        // Check for local data without user ID
        val hasLocalDataWithoutUserIdUseCase: HasLocalDataWithoutUserIdUseCase by inject()
        applicationScope.launch(Dispatchers.IO) {
            try {
                val hasLocalData = hasLocalDataWithoutUserIdUseCase()
                if (hasLocalData) {
                    Log.i(TAG, "Local data without user ID detected. Team exists without coachId.")
                } else {
                    Log.d(TAG, "No local data without user ID found.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for local data without user ID", e)
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

    companion object {
        private const val TAG = "TeamFlowManagerApp"
    }
}
