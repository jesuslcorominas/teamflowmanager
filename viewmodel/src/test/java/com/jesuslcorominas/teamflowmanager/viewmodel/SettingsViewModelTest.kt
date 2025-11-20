package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.usecase.ExportDatabaseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ImportDatabaseUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var exportDatabaseUseCase: ExportDatabaseUseCase
    private lateinit var importDatabaseUseCase: ImportDatabaseUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        exportDatabaseUseCase = mockk()
        importDatabaseUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        viewModel = SettingsViewModel(exportDatabaseUseCase, importDatabaseUseCase, analyticsTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial export result should be null`() {
        // Then
        assertNull(viewModel.exportResult.value)
    }

    @Test
    fun `initial import result should be null`() {
        // Then
        assertNull(viewModel.importResult.value)
    }

    @Test
    fun `exportData should call use case and update result on success`() = runTest {
        // Given
        val fileUri = "content://com.example.provider/file.tfm"
        coEvery { exportDatabaseUseCase() } returns fileUri

        // When
        viewModel.exportData()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { exportDatabaseUseCase() }
        verify(exactly = 1) { analyticsTracker.logEvent(any(), any()) }
        assertNotNull(viewModel.exportResult.value)
        assertTrue(viewModel.exportResult.value!!.isSuccess)
        assertEquals(fileUri, viewModel.exportResult.value!!.getOrNull())
    }

    @Test
    fun `exportData should update result on failure when use case returns null`() = runTest {
        // Given
        coEvery { exportDatabaseUseCase() } returns null

        // When
        viewModel.exportData()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { exportDatabaseUseCase() }
        verify(exactly = 1) { analyticsTracker.logEvent(any(), any()) }
        assertNotNull(viewModel.exportResult.value)
        assertTrue(viewModel.exportResult.value!!.isFailure)
    }

    @Test
    fun `exportData should update result on exception`() = runTest {
        // Given
        val exception = Exception("Export failed")
        coEvery { exportDatabaseUseCase() } throws exception

        // When
        viewModel.exportData()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { exportDatabaseUseCase() }
        verify(exactly = 1) { analyticsTracker.logEvent(any(), any()) }
        assertNotNull(viewModel.exportResult.value)
        assertTrue(viewModel.exportResult.value!!.isFailure)
        assertEquals(exception, viewModel.exportResult.value!!.exceptionOrNull())
    }

    @Test
    fun `importData should call use case and update result on success`() = runTest {
        // Given
        val fileUri = "content://com.example.provider/file.tfm"
        val source = "deep_link"
        coEvery { importDatabaseUseCase(fileUri) } returns true

        // When
        viewModel.importData(fileUri, source)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { importDatabaseUseCase(fileUri) }
        verify(exactly = 1) { analyticsTracker.logEvent(any(), any()) }
        assertNotNull(viewModel.importResult.value)
        assertTrue(viewModel.importResult.value!!.isSuccess)
        assertTrue(viewModel.importResult.value!!.getOrNull() == true)
    }

    @Test
    fun `importData should update result on failure when use case returns false`() = runTest {
        // Given
        val fileUri = "content://com.example.provider/file.tfm"
        val source = "settings_screen"
        coEvery { importDatabaseUseCase(fileUri) } returns false

        // When
        viewModel.importData(fileUri, source)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { importDatabaseUseCase(fileUri) }
        verify(exactly = 1) { analyticsTracker.logEvent(any(), any()) }
        assertNotNull(viewModel.importResult.value)
        assertTrue(viewModel.importResult.value!!.isFailure)
    }

    @Test
    fun `importData should update result on exception`() = runTest {
        // Given
        val fileUri = "content://com.example.provider/file.tfm"
        val source = "deep_link"
        val exception = Exception("Import failed")
        coEvery { importDatabaseUseCase(fileUri) } throws exception

        // When
        viewModel.importData(fileUri, source)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { importDatabaseUseCase(fileUri) }
        verify(exactly = 1) { analyticsTracker.logEvent(any(), any()) }
        assertNotNull(viewModel.importResult.value)
        assertTrue(viewModel.importResult.value!!.isFailure)
        assertEquals(exception, viewModel.importResult.value!!.exceptionOrNull())
    }

    @Test
    fun `trackImportCancelled should log analytics event`() {
        // Given
        val source = "settings_screen"

        // When
        viewModel.trackImportCancelled(source)

        // Then
        verify(exactly = 1) { analyticsTracker.logEvent(any(), any()) }
    }

    @Test
    fun `clearExportResult should set export result to null`() = runTest {
        // Given
        val fileUri = "content://com.example.provider/file.tfm"
        coEvery { exportDatabaseUseCase() } returns fileUri
        viewModel.exportData()
        advanceUntilIdle()

        // When
        viewModel.clearExportResult()

        // Then
        assertNull(viewModel.exportResult.value)
    }

    @Test
    fun `clearImportResult should set import result to null`() = runTest {
        // Given
        val fileUri = "content://com.example.provider/file.tfm"
        val source = "deep_link"
        coEvery { importDatabaseUseCase(fileUri) } returns true
        viewModel.importData(fileUri, source)
        advanceUntilIdle()

        // When
        viewModel.clearImportResult()

        // Then
        assertNull(viewModel.importResult.value)
    }
}
