package com.jesuslcorominas.teamflowmanager

import android.app.Application
import com.jesuslcorominas.teamflowmanager.di.appModule
import com.jesuslcorominas.teamflowmanager.di.teamFlowManagerModule
import com.jesuslcorominas.teamflowmanager.service.MatchNotificationServiceManager
import com.jesuslcorominas.teamflowmanager.usecase.GetActiveMatchUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TeamFlowManagerApplication : Application() {
    private val getActiveMatchUseCase: GetActiveMatchUseCase by inject()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var notificationServiceManager: MatchNotificationServiceManager

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TeamFlowManagerApplication)
            modules(appModule, teamFlowManagerModule)
        }

        // Start observing active matches for notification
        notificationServiceManager =
            MatchNotificationServiceManager(
                context = this,
                getActiveMatchUseCase = getActiveMatchUseCase,
                scope = applicationScope,
            )
        notificationServiceManager.start()
    }

    override fun onTerminate() {
        super.onTerminate()
        notificationServiceManager.stop()
    }
}
