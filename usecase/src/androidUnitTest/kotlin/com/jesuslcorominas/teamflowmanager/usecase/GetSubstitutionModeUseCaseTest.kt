package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetSubstitutionModeUseCaseTest {
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var getSubstitutionModeUseCase: GetSubstitutionModeUseCase

    @Before
    fun setup() {
        preferencesRepository = mockk(relaxed = true)
        getSubstitutionModeUseCase = GetSubstitutionModeUseCaseImpl(preferencesRepository)
    }

    @Test
    fun `givenStoredLive_whenInvoke_thenEmitsLive`() =
        runTest {
            // Given
            every { preferencesRepository.observeSubstitutionMode() } returns flowOf("LIVE")

            // When
            val result = getSubstitutionModeUseCase().toList()

            // Then
            assertEquals(1, result.size)
            assertEquals(SubstitutionMode.LIVE, result[0])
            verify { preferencesRepository.observeSubstitutionMode() }
        }

    @Test
    fun `givenStoredScheduled_whenInvoke_thenEmitsScheduled`() =
        runTest {
            // Given
            every { preferencesRepository.observeSubstitutionMode() } returns flowOf("SCHEDULED")

            // When
            val result = getSubstitutionModeUseCase().toList()

            // Then
            assertEquals(listOf(SubstitutionMode.SCHEDULED), result)
            verify { preferencesRepository.observeSubstitutionMode() }
        }

    @Test
    fun `givenNothingStored_whenInvoke_thenEmitsScheduledAsProductDefault`() =
        runTest {
            // Given — no value has ever been written to key-value storage
            every { preferencesRepository.observeSubstitutionMode() } returns flowOf(null)

            // When
            val result = getSubstitutionModeUseCase().toList()

            // Then
            assertEquals(listOf(SubstitutionMode.SCHEDULED), result)
        }

    @Test
    fun `givenStoredLowercaseLive_whenInvoke_thenFallsBackToScheduled`() =
        runTest {
            // Given — the persisted contract is SubstitutionMode.name, which is upper case:
            // a casing regression must not be read back as LIVE
            every { preferencesRepository.observeSubstitutionMode() } returns flowOf("live")

            // When
            val result = getSubstitutionModeUseCase().toList()

            // Then
            assertEquals(listOf(SubstitutionMode.SCHEDULED), result)
        }

    @Test
    fun `givenStoredGarbageValue_whenInvoke_thenFallsBackToScheduled`() =
        runTest {
            // Given
            every { preferencesRepository.observeSubstitutionMode() } returns flowOf("BOGUS")

            // When
            val result = getSubstitutionModeUseCase().toList()

            // Then
            assertEquals(listOf(SubstitutionMode.SCHEDULED), result)
        }

    @Test
    fun `givenStoredValueChanges_whenInvoke_thenEmitsAgainWithoutResubscribing`() =
        runTest {
            // Given
            val stored = MutableStateFlow<String?>("SCHEDULED")
            every { preferencesRepository.observeSubstitutionMode() } returns stored

            // When
            val emissions = mutableListOf<SubstitutionMode>()
            backgroundScope.launch {
                getSubstitutionModeUseCase().collect { emissions.add(it) }
            }
            runCurrent()
            stored.value = "LIVE"
            runCurrent()

            // Then
            assertEquals(listOf(SubstitutionMode.SCHEDULED, SubstitutionMode.LIVE), emissions)
            verify(exactly = 1) { preferencesRepository.observeSubstitutionMode() }
        }
}
