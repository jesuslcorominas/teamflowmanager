package com.jesuslcorominas.teamflowmanager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.provider.Settings
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
        createNotificationChannels()
        startKoin {
            androidContext(this@TeamFlowManagerApplication)
            modules(appModule, teamFlowManagerModule)
        }
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(
            PUSH_CHANNEL_ID,
            "Notificaciones del club",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notificaciones de eventos de partido y club"
            setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val PUSH_CHANNEL_ID = "push_notifications_v3"
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .logger(DebugLogger())
            .build()
}
