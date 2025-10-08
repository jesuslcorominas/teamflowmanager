package com.jesuslcorominas.teamflowmanager

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TeamFlowManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TeamFlowManagerApplication)
        }
    }
}
