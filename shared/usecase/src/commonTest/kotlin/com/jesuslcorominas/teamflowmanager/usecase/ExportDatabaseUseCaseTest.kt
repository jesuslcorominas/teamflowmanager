package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ExportDatabaseUseCaseTest {
    private lateinit var databaseExporter: DatabaseExporter
    private lateinit var useCase: ExportDatabaseUseCase

    @Before
    fun setup() {
        databaseExporter = mockk()
        useCase = ExportDatabaseUseCaseImpl(databaseExporter)
    }

    @Test
    fun `invoke should call exporter exportDatabase and return file uri`() = runTest {
        // Given
        val expectedUri = "content://com.example.provider/file.tfm"
        coEvery { databaseExporter.exportDatabase() } returns expectedUri

        // When
        val result = useCase()

        // Then
        coVerify { databaseExporter.exportDatabase() }
        assertEquals(expectedUri, result)
    }

    @Test
    fun `invoke should return null when export fails`() = runTest {
        // Given
        coEvery { databaseExporter.exportDatabase() } returns null

        // When
        val result = useCase()

        // Then
        coVerify { databaseExporter.exportDatabase() }
        assertNull(result)
    }
}
