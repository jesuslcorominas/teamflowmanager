package com.jesuslcorominas.teamflowmanager.data.core.datasource

interface NotificationPermissionDataSource {
    fun isGranted(): Boolean
}
