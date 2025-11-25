package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseImporter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportDatabaseUseCaseTest {
    private lateinit var databaseImporter: DatabaseImporter
    private lateinit var useCase: ImportDatabaseUseCase

    @Before
    fun setup() {
        databaseImporter = mockk()
        useCase = ImportDatabaseUseCaseImpl(databaseImporter)
    }

    @Test
    fun `invoke should call importer importDatabase and return success`() = runTest {
        // Given
        val fileUri = "content://com.example.provider/file.tfm"
        coEvery { databaseImporter.importDatabase(fileUri) } returns true

        // When
        val result = useCase(fileUri)

        // Then
        coVerify { databaseImporter.importDatabase(fileUri) }
        assertTrue(result)
    }

    @Test
    fun `invoke should return false when import fails`() = runTest {
        // Given
        val fileUri = "content://com.example.provider/file.tfm"
        coEvery { databaseImporter.importDatabase(fileUri) } returns false

        // When
        val result = useCase(fileUri)

        // Then
        coVerify { databaseImporter.importDatabase(fileUri) }
        assertFalse(result)
    }
}
