package com.jesuslcorominas.teamflowmanager

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import com.jesuslcorominas.teamflowmanager.di.appModule
import com.jesuslcorominas.teamflowmanager.di.teamFlowManagerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TeamFlowManagerApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TeamFlowManagerApplication)
            modules(appModule, teamFlowManagerModule)
        }
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .logger(DebugLogger())
            .build()
}
