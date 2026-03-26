package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPermissionDataSource

class NotificationPermissionDataSourceImpl(
    private val context: Context,
) : NotificationPermissionDataSource {
    override fun isGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
}
