package com.jesuslcorominas.teamflowmanager.usecase

import android.content.Context
import android.net.Uri
import com.jesuslcorominas.teamflowmanager.usecase.repository.DatabaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ImportDatabaseUseCaseTest {
    private lateinit var databaseRepository: DatabaseRepository
    private lateinit var useCase: ImportDatabaseUseCase

    @Before
    fun setup() {
        databaseRepository = mockk()
        useCase = ImportDatabaseUseCaseImpl(databaseRepository)
    }

    @Test
    fun `invoke should call repository importDatabase`() = runTest {
        // Given
        val context = mockk<Context>()
        val uri = mockk<Uri>()
        coEvery { databaseRepository.importDatabase(context, uri) } just runs

        // When
        useCase(context, uri)

        // Then
        coVerify { databaseRepository.importDatabase(context, uri) }
    }
}
