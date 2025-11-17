package com.jesuslcorominas.teamflowmanager.viewmodel

import android.content.Context
import android.net.Uri
import com.jesuslcorominas.teamflowmanager.usecase.ExportDatabaseUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ImportDatabaseUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        exportDatabaseUseCase = mockk()
        importDatabaseUseCase = mockk()
        viewModel = SettingsViewModel(exportDatabaseUseCase, importDatabaseUseCase)
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
        val context = mockk<Context>()
        val uri = mockk<Uri>()
        coEvery { exportDatabaseUseCase(context, uri) } just runs

        // When
        viewModel.exportData(context, uri)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { exportDatabaseUseCase(context, uri) }
        assertNotNull(viewModel.exportResult.value)
        assertTrue(viewModel.exportResult.value!!.isSuccess)
    }

    @Test
    fun `exportData should update result on failure`() = runTest {
        // Given
        val context = mockk<Context>()
        val uri = mockk<Uri>()
        val exception = Exception("Export failed")
        coEvery { exportDatabaseUseCase(context, uri) } throws exception

        // When
        viewModel.exportData(context, uri)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { exportDatabaseUseCase(context, uri) }
        assertNotNull(viewModel.exportResult.value)
        assertTrue(viewModel.exportResult.value!!.isFailure)
        assertEquals(exception, viewModel.exportResult.value!!.exceptionOrNull())
    }

    @Test
    fun `importData should call use case and update result on success`() = runTest {
        // Given
        val context = mockk<Context>()
        val uri = mockk<Uri>()
        coEvery { importDatabaseUseCase(context, uri) } just runs

        // When
        viewModel.importData(context, uri)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { importDatabaseUseCase(context, uri) }
        assertNotNull(viewModel.importResult.value)
        assertTrue(viewModel.importResult.value!!.isSuccess)
    }

    @Test
    fun `importData should update result on failure`() = runTest {
        // Given
        val context = mockk<Context>()
        val uri = mockk<Uri>()
        val exception = Exception("Import failed")
        coEvery { importDatabaseUseCase(context, uri) } throws exception

        // When
        viewModel.importData(context, uri)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { importDatabaseUseCase(context, uri) }
        assertNotNull(viewModel.importResult.value)
        assertTrue(viewModel.importResult.value!!.isFailure)
        assertEquals(exception, viewModel.importResult.value!!.exceptionOrNull())
    }

    @Test
    fun `clearExportResult should set export result to null`() = runTest {
        // Given
        val context = mockk<Context>()
        val uri = mockk<Uri>()
        coEvery { exportDatabaseUseCase(context, uri) } just runs
        viewModel.exportData(context, uri)
        advanceUntilIdle()

        // When
        viewModel.clearExportResult()

        // Then
        assertNull(viewModel.exportResult.value)
    }

    @Test
    fun `clearImportResult should set import result to null`() = runTest {
        // Given
        val context = mockk<Context>()
        val uri = mockk<Uri>()
        coEvery { importDatabaseUseCase(context, uri) } just runs
        viewModel.importData(context, uri)
        advanceUntilIdle()

        // When
        viewModel.clearImportResult()

        // Then
        assertNull(viewModel.importResult.value)
    }
}
