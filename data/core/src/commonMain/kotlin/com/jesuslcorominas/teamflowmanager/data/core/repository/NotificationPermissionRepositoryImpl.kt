package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPermissionDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPermissionRepository

class NotificationPermissionRepositoryImpl(
    private val dataSource: NotificationPermissionDataSource,
) : NotificationPermissionRepository {
    override fun isGranted(): Boolean = dataSource.isGranted()
}
