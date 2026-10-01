package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SetSubstitutionModeUseCaseTest {
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var setSubstitutionModeUseCase: SetSubstitutionModeUseCase

    @Before
    fun setup() {
        preferencesRepository = mockk(relaxed = true)
        setSubstitutionModeUseCase = SetSubstitutionModeUseCaseImpl(preferencesRepository)
    }

    @Test
    fun `givenLiveMode_whenInvoke_thenPersistsTheExactStringLIVE`() =
        runTest {
            // When
            setSubstitutionModeUseCase(SubstitutionMode.LIVE)

            // Then — the literal persisted value is the contract with storage, not just "some string"
            verify(exactly = 1) { preferencesRepository.setSubstitutionMode("LIVE") }
        }

    @Test
    fun `givenScheduledMode_whenInvoke_thenPersistsTheExactStringSCHEDULED`() =
        runTest {
            // When
            setSubstitutionModeUseCase(SubstitutionMode.SCHEDULED)

            // Then
            verify(exactly = 1) { preferencesRepository.setSubstitutionMode("SCHEDULED") }
        }
}
