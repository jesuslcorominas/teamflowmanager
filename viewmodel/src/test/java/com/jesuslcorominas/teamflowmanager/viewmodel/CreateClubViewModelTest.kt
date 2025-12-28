package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreateClubUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateClubViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var createClubUseCase: CreateClubUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var viewModel: CreateClubViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        createClubUseCase = mockk()
        analyticsTracker = mockk(relaxed = true)
        viewModel = CreateClubViewModel(createClubUseCase, analyticsTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        // Then
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
        assertEquals("", viewModel.clubName.value)
        assertNull(viewModel.clubNameError.value)
    }

    @Test
    fun `onClubNameChanged should update club name and clear error`() = runTest {
        // Given
        viewModel.clubNameError.value?.let { /* Error exists */ }

        // When
        viewModel.onClubNameChanged("Test Club")
        advanceUntilIdle()

        // Then
        assertEquals("Test Club", viewModel.clubName.value)
        assertNull(viewModel.clubNameError.value)
    }

    @Test
    fun `createClub should show error when club name is empty`() = runTest {
        // Given
        viewModel.onClubNameChanged("")

        // When
        viewModel.createClub()
        advanceUntilIdle()

        // Then
        assertEquals(com.jesuslcorominas.teamflowmanager.R.string.club_name_error_empty, viewModel.clubNameError.value)
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { createClubUseCase(any()) }
    }

    @Test
    fun `createClub should show error when club name is too short`() = runTest {
        // Given
        viewModel.onClubNameChanged("AB")

        // When
        viewModel.createClub()
        advanceUntilIdle()

        // Then
        assertEquals(com.jesuslcorominas.teamflowmanager.R.string.club_name_error_too_short, viewModel.clubNameError.value)
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { createClubUseCase(any()) }
    }

    @Test
    fun `createClub should show error when club name is too long`() = runTest {
        // Given
        val longName = "A".repeat(51)
        viewModel.onClubNameChanged(longName)

        // When
        viewModel.createClub()
        advanceUntilIdle()

        // Then
        assertEquals(com.jesuslcorominas.teamflowmanager.R.string.club_name_error_too_long, viewModel.clubNameError.value)
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { createClubUseCase(any()) }
    }

    @Test
    fun `createClub should succeed with valid club name`() = runTest {
        // Given
        val clubName = "Test Club"
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = clubName,
            invitationCode = "ABC12345",
            firestoreId = "club123"
        )
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } returns expectedClub

        // When
        viewModel.createClub()
        advanceUntilIdle()

        // Then
        assertEquals(CreateClubViewModel.UiState.Success(expectedClub), viewModel.uiState.value)
        coVerify { createClubUseCase(clubName) }
        verify {
            analyticsTracker.logEvent(
                AnalyticsEvent.CLUB_CREATED,
                mapOf(
                    AnalyticsParam.CLUB_ID to expectedClub.id.toString(),
                    AnalyticsParam.CLUB_NAME to expectedClub.name
                )
            )
        }
    }

    @Test
    fun `createClub should emit Loading state while creating`() = runTest {
        // Given
        val clubName = "Test Club"
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = clubName,
            invitationCode = "ABC12345",
            firestoreId = "club123"
        )
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } coAnswers {
            // Simulate delay
            kotlinx.coroutines.delay(100)
            expectedClub
        }

        // When
        viewModel.createClub()
        
        // Then - should be loading before completion
        assertEquals(CreateClubViewModel.UiState.Loading, viewModel.uiState.value)
        
        // Wait for completion
        advanceUntilIdle()
        
        // Then - should be success after completion
        assertEquals(CreateClubViewModel.UiState.Success(expectedClub), viewModel.uiState.value)
    }

    @Test
    fun `createClub should emit Error state on failure`() = runTest {
        // Given
        val clubName = "Test Club"
        val errorMessage = "Network error"
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } throws Exception(errorMessage)

        // When
        viewModel.createClub()
        advanceUntilIdle()

        // Then
        assertEquals(CreateClubViewModel.UiState.Error(errorMessage), viewModel.uiState.value)
        verify {
            analyticsTracker.logEvent(
                AnalyticsEvent.CLUB_CREATION_ERROR,
                mapOf(AnalyticsParam.ERROR_MESSAGE to errorMessage)
            )
        }
    }

    @Test
    fun `createClub should trim whitespace from club name`() = runTest {
        // Given
        val clubName = "  Test Club  "
        val expectedClub = Club(
            id = 1L,
            ownerId = "user123",
            name = "Test Club",
            invitationCode = "ABC12345",
            firestoreId = "club123"
        )
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase("Test Club") } returns expectedClub

        // When
        viewModel.createClub()
        advanceUntilIdle()

        // Then
        coVerify { createClubUseCase("Test Club") }
        assertEquals(CreateClubViewModel.UiState.Success(expectedClub), viewModel.uiState.value)
    }

    @Test
    fun `resetState should return to Idle`() = runTest {
        // Given
        val clubName = "Test Club"
        viewModel.onClubNameChanged(clubName)
        coEvery { createClubUseCase(clubName) } throws Exception("Error")
        viewModel.createClub()
        advanceUntilIdle()

        // When
        viewModel.resetState()

        // Then
        assertEquals(CreateClubViewModel.UiState.Idle, viewModel.uiState.value)
    }
}
