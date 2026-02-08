package com.jesuslcorominas.teamflowmanager.data.local.datasource

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PreferencesLocalDataSourceImplTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var dataSource: PreferencesLocalDataSourceImpl

    @Before
    fun setup() {
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk()

        every { context.getSharedPreferences("teamflowmanager_preferences", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor

        // Mock chaining
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        dataSource = PreferencesLocalDataSourceImpl(context)
    }

    @Test
    fun `shouldShowInvalidSubstitutionAlert should return value from shared preferences`() {
        // Given
        every { sharedPreferences.getBoolean("show_invalid_substitution_alert", true) } returns false

        // When
        val result = dataSource.shouldShowInvalidSubstitutionAlert()

        // Then
        assertEquals(false, result)
    }

    @Test
    fun `setShouldShowInvalidSubstitutionAlert should save value to shared preferences`() {
        // When
        dataSource.setShouldShowInvalidSubstitutionAlert(false)

        // Then
        verify { editor.putBoolean("show_invalid_substitution_alert", false) }
        verify { editor.apply() }
    }

    @Test
    fun `getDefaultCaptainId should return value from shared preferences`() {
        // Given
        every { sharedPreferences.getLong("default_captain_id", -1L) } returns 10L

        // When
        val result = dataSource.getDefaultCaptainId()

        // Then
        assertEquals(10L, result)
    }

    @Test
    fun `getDefaultCaptainId should return null if no value in shared preferences`() {
        // Given
        every { sharedPreferences.getLong("default_captain_id", -1L) } returns -1L

        // When
        val result = dataSource.getDefaultCaptainId()

        // Then
        assertNull(result)
    }

    @Test
    fun `setDefaultCaptainId should save value to shared preferences`() {
        // When
        dataSource.setDefaultCaptainId(10L)

        // Then
        verify { editor.putLong("default_captain_id", 10L) }
        verify { editor.apply() }
    }

    @Test
    fun `setDefaultCaptainId with null should save -1L to shared preferences`() {
        // When
        dataSource.setDefaultCaptainId(null)

        // Then
        verify { editor.putLong("default_captain_id", -1L) }
        verify { editor.apply() }
    }

    @Test
    fun `hasNotificationPermissionBeenRequested should return value from shared preferences`() {
        // Given
        every { sharedPreferences.getBoolean("notification_permission_requested", false) } returns true

        // When
        val result = dataSource.hasNotificationPermissionBeenRequested()

        // Then
        assertEquals(true, result)
    }

    @Test
    fun `setNotificationPermissionRequested should save value to shared preferences`() {
        // When
        dataSource.setNotificationPermissionRequested(true)

        // Then
        verify { editor.putBoolean("notification_permission_requested", true) }
        verify { editor.apply() }
    }
}
