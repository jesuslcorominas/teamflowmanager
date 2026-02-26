package com.jesuslcorominas.teamflowmanager.data.local.datasource

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PreferencesLocalDataSourceImplTest {

    // SharedPreferences.Editor is a builder — relaxed mock justified to handle
    // the fluent chain (putBoolean/putLong return the editor itself, apply() is void)
    private val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
    private val mockSharedPreferences: SharedPreferences = mockk()
    private val mockContext: Context = mockk()

    private lateinit var dataSource: PreferencesLocalDataSourceImpl

    @Before
    fun setup() {
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor

        dataSource = PreferencesLocalDataSourceImpl(mockContext)
    }

    // --- shouldShowInvalidSubstitutionAlert ---

    @Test
    fun `givenPreferenceNotSet_whenShouldShowInvalidSubstitutionAlert_thenReturnsDefaultTrue`() {
        every { mockSharedPreferences.getBoolean("show_invalid_substitution_alert", true) } returns true

        val result = dataSource.shouldShowInvalidSubstitutionAlert()

        assertTrue(result)
    }

    @Test
    fun `givenAlertDisabledByUser_whenShouldShowInvalidSubstitutionAlert_thenReturnsFalse`() {
        every { mockSharedPreferences.getBoolean("show_invalid_substitution_alert", true) } returns false

        val result = dataSource.shouldShowInvalidSubstitutionAlert()

        assertFalse(result)
    }

    // --- setShouldShowInvalidSubstitutionAlert ---

    @Test
    fun `givenTrueValue_whenSetShouldShowInvalidSubstitutionAlert_thenStoresTrueInPreferences`() {
        dataSource.setShouldShowInvalidSubstitutionAlert(true)

        verify { mockEditor.putBoolean("show_invalid_substitution_alert", true) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `givenFalseValue_whenSetShouldShowInvalidSubstitutionAlert_thenStoresFalseInPreferences`() {
        dataSource.setShouldShowInvalidSubstitutionAlert(false)

        verify { mockEditor.putBoolean("show_invalid_substitution_alert", false) }
        verify { mockEditor.apply() }
    }

    // --- getDefaultCaptainId ---

    @Test
    fun `givenCaptainIdStored_whenGetDefaultCaptainId_thenReturnsCaptainId`() {
        every { mockSharedPreferences.getLong("default_captain_id", -1L) } returns 7L

        val result = dataSource.getDefaultCaptainId()

        assertEquals(7L, result)
    }

    @Test
    fun `givenSentinelValueStored_whenGetDefaultCaptainId_thenReturnsNull`() {
        every { mockSharedPreferences.getLong("default_captain_id", -1L) } returns -1L

        val result = dataSource.getDefaultCaptainId()

        assertNull(result)
    }

    // --- setDefaultCaptainId ---

    @Test
    fun `givenCaptainId_whenSetDefaultCaptainId_thenStoresCaptainIdInPreferences`() {
        dataSource.setDefaultCaptainId(5L)

        verify { mockEditor.putLong("default_captain_id", 5L) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `givenNullCaptainId_whenSetDefaultCaptainId_thenStoresSentinelValueInPreferences`() {
        dataSource.setDefaultCaptainId(null)

        verify { mockEditor.putLong("default_captain_id", -1L) }
        verify { mockEditor.apply() }
    }

    // --- hasNotificationPermissionBeenRequested ---

    @Test
    fun `givenPermissionNeverRequested_whenHasNotificationPermissionBeenRequested_thenReturnsDefaultFalse`() {
        every { mockSharedPreferences.getBoolean("notification_permission_requested", false) } returns false

        val result = dataSource.hasNotificationPermissionBeenRequested()

        assertFalse(result)
    }

    @Test
    fun `givenPermissionAlreadyRequested_whenHasNotificationPermissionBeenRequested_thenReturnsTrue`() {
        every { mockSharedPreferences.getBoolean("notification_permission_requested", false) } returns true

        val result = dataSource.hasNotificationPermissionBeenRequested()

        assertTrue(result)
    }

    // --- setNotificationPermissionRequested ---

    @Test
    fun `givenTrueValue_whenSetNotificationPermissionRequested_thenStoresTrueInPreferences`() {
        dataSource.setNotificationPermissionRequested(true)

        verify { mockEditor.putBoolean("notification_permission_requested", true) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `givenFalseValue_whenSetNotificationPermissionRequested_thenStoresFalseInPreferences`() {
        dataSource.setNotificationPermissionRequested(false)

        verify { mockEditor.putBoolean("notification_permission_requested", false) }
        verify { mockEditor.apply() }
    }
}
