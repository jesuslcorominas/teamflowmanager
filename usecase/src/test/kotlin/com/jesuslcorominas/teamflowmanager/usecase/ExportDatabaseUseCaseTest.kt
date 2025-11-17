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

class ExportDatabaseUseCaseTest {
    private lateinit var databaseRepository: DatabaseRepository
    private lateinit var useCase: ExportDatabaseUseCase

    @Before
    fun setup() {
        databaseRepository = mockk()
        useCase = ExportDatabaseUseCaseImpl(databaseRepository)
    }

    @Test
    fun `invoke should call repository exportDatabase`() = runTest {
        // Given
        val context = mockk<Context>()
        val uri = mockk<Uri>()
        coEvery { databaseRepository.exportDatabase(context, uri) } just runs

        // When
        useCase(context, uri)

        // Then
        coVerify { databaseRepository.exportDatabase(context, uri) }
    }
}
