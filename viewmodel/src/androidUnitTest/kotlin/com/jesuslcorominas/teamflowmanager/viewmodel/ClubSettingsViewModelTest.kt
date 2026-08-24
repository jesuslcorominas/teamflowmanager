package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegenerateInvitationCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateClubUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClubSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getUserClubMembership: GetUserClubMembershipUseCase
    private lateinit var getClubById: GetClubByIdUseCase
    private lateinit var updateClubUseCase: UpdateClubUseCase
    private lateinit var regenerateInvitationCodeUseCase: RegenerateInvitationCodeUseCase

    private val clubId = "club-1"
    private val club = Club(
        id = clubId,
        ownerId = "owner-1",
        name = "My Club",
        invitationCode = "ABC123",
        homeGround = "Home Stadium",
    )
    private val member = ClubMember(
        id = "member-1",
        userId = "user-1",
        name = "John Doe",
        email = "john@example.com",
        clubId = clubId,
        roles = listOf("President"),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getUserClubMembership = mockk()
        getClubById = mockk()
        updateClubUseCase = mockk()
        regenerateInvitationCodeUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ClubSettingsViewModel = ClubSettingsViewModel(
        getUserClubMembership = getUserClubMembership,
        getClubById = getClubById,
        updateClubUseCase = updateClubUseCase,
        regenerateInvitationCodeUseCase = regenerateInvitationCodeUseCase,
    )

    // ── loadClub ──────────────────────────────────────────────────────────────

    @Test
    fun `loadClub success populates uiState and clears loading`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("My Club", state.name)
        assertEquals("Home Stadium", state.homeGround)
        assertEquals("ABC123", state.invitationCode)
        assertNull(state.error)
    }

    @Test
    fun `loadClub when membership is null sets error state`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("No club membership found", state.error)
    }

    @Test
    fun `loadClub when club is null sets error state`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns null

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("Club not found", state.error)
    }

    @Test
    fun `loadClub when exception is thrown sets error state`() = runTest(testDispatcher) {
        every { getUserClubMembership() } throws RuntimeException("Network error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `loadClub club homeGround null is treated as empty string`() = runTest(testDispatcher) {
        val clubNoGround = club.copy(homeGround = null)
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns clubNoGround

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.homeGround)
    }

    // ── edit mode ─────────────────────────────────────────────────────────────

    @Test
    fun `onEnterEdit sets isEditing to true`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEnterEdit()

        assertTrue(viewModel.uiState.value.isEditing)
    }

    @Test
    fun `onCancelEdit without changes sets isEditing to false directly`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEnterEdit()
        // No name/homeGround changes
        viewModel.onCancelEdit()

        val state = viewModel.uiState.value
        assertFalse(state.isEditing)
        assertFalse(state.showExitDialog)
    }

    @Test
    fun `onCancelEdit with unsaved name change shows exit dialog`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEnterEdit()
        viewModel.onNameChange("New Name")
        viewModel.onCancelEdit()

        val state = viewModel.uiState.value
        assertTrue(state.showExitDialog)
        assertTrue(state.isEditing)
    }

    @Test
    fun `onCancelEdit with unsaved homeGround change shows exit dialog`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEnterEdit()
        viewModel.onHomeGroundChange("New Ground")
        viewModel.onCancelEdit()

        assertTrue(viewModel.uiState.value.showExitDialog)
    }

    @Test
    fun `onConfirmExit restores saved values and exits edit mode`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEnterEdit()
        viewModel.onNameChange("Changed Name")
        viewModel.onHomeGroundChange("Changed Ground")
        viewModel.onConfirmExit()

        val state = viewModel.uiState.value
        assertEquals("My Club", state.name)
        assertEquals("Home Stadium", state.homeGround)
        assertFalse(state.isEditing)
        assertFalse(state.showExitDialog)
        assertNull(state.error)
    }

    @Test
    fun `onDismissExitDialog hides the exit dialog`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEnterEdit()
        viewModel.onNameChange("Changed")
        viewModel.onCancelEdit() // triggers showExitDialog = true
        viewModel.onDismissExitDialog()

        assertFalse(viewModel.uiState.value.showExitDialog)
    }

    // ── field changes ─────────────────────────────────────────────────────────

    @Test
    fun `onNameChange updates name and clears error`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onNameChange("Updated Club Name")

        val state = viewModel.uiState.value
        assertEquals("Updated Club Name", state.name)
        assertNull(state.error)
    }

    @Test
    fun `onHomeGroundChange updates homeGround and clears error`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onHomeGroundChange("New Stadium")

        val state = viewModel.uiState.value
        assertEquals("New Stadium", state.homeGround)
        assertNull(state.error)
    }

    // ── onSave ────────────────────────────────────────────────────────────────

    @Test
    fun `onSave success updates state and exits edit mode`() = runTest(testDispatcher) {
        val updatedClub = club.copy(name = "Updated Club", homeGround = "New Ground")
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club
        coEvery { updateClubUseCase(clubId, "Updated Club", "New Ground") } returns updatedClub

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEnterEdit()
        viewModel.onNameChange("Updated Club")
        viewModel.onHomeGroundChange("New Ground")
        viewModel.onSave()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Updated Club", state.name)
        assertEquals("New Ground", state.homeGround)
        assertFalse(state.saving)
        assertTrue(state.saved)
        assertFalse(state.isEditing)
        assertNull(state.error)
    }

    @Test
    fun `onSave with blank name does nothing`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onNameChange("   ")
        viewModel.onSave()
        advanceUntilIdle()

        // updateClubUseCase should not be called — state unchanged
        assertFalse(viewModel.uiState.value.saving)
        assertFalse(viewModel.uiState.value.saved)
    }

    @Test
    fun `onSave when exception is thrown sets error state`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club
        coEvery { updateClubUseCase(any(), any(), any()) } throws RuntimeException("Save failed")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSave()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.saving)
        assertEquals("Save failed", state.error)
    }

    @Test
    fun `onSave with empty homeGround passes null to use case`() = runTest(testDispatcher) {
        val updatedClub = club.copy(homeGround = null)
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club
        coEvery { updateClubUseCase(clubId, "My Club", null) } returns updatedClub

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onHomeGroundChange("")
        viewModel.onSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saved)
    }

    // ── resetSavedState ───────────────────────────────────────────────────────

    @Test
    fun `resetSavedState clears saved flag`() = runTest(testDispatcher) {
        val updatedClub = club.copy(name = "Updated")
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club
        coEvery { updateClubUseCase(any(), any(), any()) } returns updatedClub

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saved)

        viewModel.resetSavedState()

        assertFalse(viewModel.uiState.value.saved)
    }

    // ── onRegenerateCode ──────────────────────────────────────────────────────

    @Test
    fun `onRegenerateCode success updates invitation code`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club
        coEvery { regenerateInvitationCodeUseCase(clubId) } returns "NEW_CODE_XYZ"

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRegenerateCode()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("NEW_CODE_XYZ", state.invitationCode)
        assertFalse(state.regenerating)
        assertNull(state.error)
    }

    @Test
    fun `onRegenerateCode when exception is thrown sets error state`() = runTest(testDispatcher) {
        every { getUserClubMembership() } returns flowOf(member)
        coEvery { getClubById(clubId) } returns club
        coEvery { regenerateInvitationCodeUseCase(clubId) } throws RuntimeException("Regeneration failed")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRegenerateCode()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.regenerating)
        assertEquals("Regeneration failed", state.error)
    }
}
