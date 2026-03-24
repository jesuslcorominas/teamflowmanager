package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PreferencesDataSource
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PreferencesRepositoryImplTest {

    private lateinit var preferencesDataSource: PreferencesDataSource
    private lateinit var repository: PreferencesRepositoryImpl

    @Before
    fun setup() {
        preferencesDataSource = mockk(relaxed = true)
        repository = PreferencesRepositoryImpl(preferencesDataSource)
    }

    // --- shouldShowInvalidSubstitutionAlert ---

    @Test
    fun `givenAlertEnabled_whenShouldShowInvalidSubstitutionAlert_thenReturnsTrue`() {
        every { preferencesDataSource.shouldShowInvalidSubstitutionAlert() } returns true

        val result = repository.shouldShowInvalidSubstitutionAlert()

        assertTrue(result)
        verify { preferencesDataSource.shouldShowInvalidSubstitutionAlert() }
    }

    @Test
    fun `givenAlertDisabled_whenShouldShowInvalidSubstitutionAlert_thenReturnsFalse`() {
        every { preferencesDataSource.shouldShowInvalidSubstitutionAlert() } returns false

        val result = repository.shouldShowInvalidSubstitutionAlert()

        assertFalse(result)
    }

    // --- setShouldShowInvalidSubstitutionAlert ---

    @Test
    fun `givenTrueValue_whenSetShouldShowInvalidSubstitutionAlert_thenDelegatesToDataSource`() {
        every { preferencesDataSource.setShouldShowInvalidSubstitutionAlert(true) } just runs

        repository.setShouldShowInvalidSubstitutionAlert(true)

        verify { preferencesDataSource.setShouldShowInvalidSubstitutionAlert(true) }
    }

    @Test
    fun `givenFalseValue_whenSetShouldShowInvalidSubstitutionAlert_thenDelegatesToDataSource`() {
        every { preferencesDataSource.setShouldShowInvalidSubstitutionAlert(false) } just runs

        repository.setShouldShowInvalidSubstitutionAlert(false)

        verify { preferencesDataSource.setShouldShowInvalidSubstitutionAlert(false) }
    }

    // --- getDefaultCaptainId ---

    @Test
    fun `givenCaptainIdSaved_whenGetDefaultCaptainId_thenReturnsCaptainId`() {
        val captainId = 5L
        every { preferencesDataSource.getDefaultCaptainId() } returns captainId

        val result = repository.getDefaultCaptainId()

        assertEquals(captainId, result)
        verify { preferencesDataSource.getDefaultCaptainId() }
    }

    @Test
    fun `givenNoCaptainIdSaved_whenGetDefaultCaptainId_thenReturnsNull`() {
        every { preferencesDataSource.getDefaultCaptainId() } returns null

        val result = repository.getDefaultCaptainId()

        assertNull(result)
    }

    // --- setDefaultCaptainId ---

    @Test
    fun `givenCaptainId_whenSetDefaultCaptainId_thenDelegatesToDataSource`() {
        val captainId = 7L
        every { preferencesDataSource.setDefaultCaptainId(captainId) } just runs

        repository.setDefaultCaptainId(captainId)

        verify { preferencesDataSource.setDefaultCaptainId(captainId) }
    }

    @Test
    fun `givenNullCaptainId_whenSetDefaultCaptainId_thenDelegatesToDataSource`() {
        every { preferencesDataSource.setDefaultCaptainId(null) } just runs

        repository.setDefaultCaptainId(null)

        verify { preferencesDataSource.setDefaultCaptainId(null) }
    }

    // --- hasNotificationPermissionBeenRequested ---

    @Test
    fun `givenPermissionAlreadyRequested_whenHasNotificationPermissionBeenRequested_thenReturnsTrue`() {
        every { preferencesDataSource.hasNotificationPermissionBeenRequested() } returns true

        val result = repository.hasNotificationPermissionBeenRequested()

        assertTrue(result)
        verify { preferencesDataSource.hasNotificationPermissionBeenRequested() }
    }

    @Test
    fun `givenPermissionNotYetRequested_whenHasNotificationPermissionBeenRequested_thenReturnsFalse`() {
        every { preferencesDataSource.hasNotificationPermissionBeenRequested() } returns false

        val result = repository.hasNotificationPermissionBeenRequested()

        assertFalse(result)
    }

    // --- setNotificationPermissionRequested ---

    @Test
    fun `givenTrueValue_whenSetNotificationPermissionRequested_thenDelegatesToDataSource`() {
        every { preferencesDataSource.setNotificationPermissionRequested(true) } just runs

        repository.setNotificationPermissionRequested(true)

        verify { preferencesDataSource.setNotificationPermissionRequested(true) }
    }

    @Test
    fun `givenFalseValue_whenSetNotificationPermissionRequested_thenDelegatesToDataSource`() {
        every { preferencesDataSource.setNotificationPermissionRequested(false) } just runs

        repository.setNotificationPermissionRequested(false)

        verify { preferencesDataSource.setNotificationPermissionRequested(false) }
    }
}
