package com.jesuslcorominas.teamflowmanager.usecase.repository

interface NotificationPermissionRepository {
    fun isGranted(): Boolean
}
