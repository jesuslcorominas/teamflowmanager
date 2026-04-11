package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePresidentNotificationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPresidentNotificationsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUnreadPresidentNotificationsCountUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.MarkPresidentNotificationAsReadUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.MarkPresidentNotificationAsUnreadUseCase
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PresidentNotificationsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getNotifications: GetPresidentNotificationsUseCase
    private lateinit var getUnreadCount: GetUnreadPresidentNotificationsCountUseCase
    private lateinit var markAsRead: MarkPresidentNotificationAsReadUseCase
    private lateinit var markAsUnread: MarkPresidentNotificationAsUnreadUseCase
    private lateinit var deleteNotification: DeletePresidentNotificationUseCase
    private lateinit var getUserClubMembership: GetUserClubMembershipUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getNotifications = mockk()
        getUnreadCount = mockk()
        markAsRead = mockk(relaxed = true)
        markAsUnread = mockk(relaxed = true)
        deleteNotification = mockk(relaxed = true)
        getUserClubMembership = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        PresidentNotificationsViewModel(
            getNotifications,
            getUnreadCount,
            markAsRead,
            markAsUnread,
            deleteNotification,
            getUserClubMembership,
        )

    private fun aClubMember() =
        ClubMember(
            id = 1L,
            userId = "user1",
            name = "Test User",
            email = "test@test.com",
            clubId = 1L,
            roles = listOf("PRESIDENT"),
            remoteId = "member1",
            clubRemoteId = "club1",
        )

    private fun aNotification(
        id: String = "notif1",
        read: Boolean = false,
    ) = PresidentNotification(
        id = id,
        type = NotificationType.USER_WAITING_FOR_ASSIGNMENT,
        title = "Test Title",
        body = "Test Body",
        userData = emptyMap(),
        createdAt = 1000L,
        read = read,
    )

    @Test
    fun `initial state is Loading before load completes`() {
        every { getUserClubMembership() } returns flowOf(aClubMember())
        every { getNotifications(any()) } returns flowOf(emptyList())
        every { getUnreadCount(any()) } returns flowOf(0)

        val viewModel = createViewModel()

        assertEquals(PresidentNotificationsViewModel.UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `when getUserClubMembership returns null membership state becomes NoClubMembership`() =
        runTest {
            every { getUserClubMembership() } returns flowOf(null)

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(
                PresidentNotificationsViewModel.UiState.NoClubMembership,
                viewModel.uiState.value,
            )
        }

    @Test
    fun `when getUserClubMembership returns member with blank clubRemoteId state becomes NoClubMembership`() =
        runTest {
            val memberWithBlankClubId = aClubMember().copy(clubRemoteId = "")
            every { getUserClubMembership() } returns flowOf(memberWithBlankClubId)

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(
                PresidentNotificationsViewModel.UiState.NoClubMembership,
                viewModel.uiState.value,
            )
        }

    @Test
    fun `when notifications loaded state becomes Success with correct list`() =
        runTest {
            val notifications = listOf(aNotification("notif1"), aNotification("notif2", read = true))
            every { getUserClubMembership() } returns flowOf(aClubMember())
            every { getNotifications("club1") } returns flowOf(notifications)
            every { getUnreadCount("club1") } returns flowOf(1)

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value as PresidentNotificationsViewModel.UiState.Success
            assertEquals(notifications, state.notifications)
        }

    @Test
    fun `when notifications loaded state becomes Success with empty list`() =
        runTest {
            every { getUserClubMembership() } returns flowOf(aClubMember())
            every { getNotifications("club1") } returns flowOf(emptyList())
            every { getUnreadCount("club1") } returns flowOf(0)

            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value as PresidentNotificationsViewModel.UiState.Success
            assertTrue(state.notifications.isEmpty())
        }

    @Test
    fun `unread count is emitted correctly`() =
        runTest {
            every { getUserClubMembership() } returns flowOf(aClubMember())
            every { getNotifications("club1") } returns flowOf(emptyList())
            every { getUnreadCount("club1") } returns flowOf(3)

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(3, viewModel.unreadCount.value)
        }

    @Test
    fun `unread count is zero when no unread notifications`() =
        runTest {
            every { getUserClubMembership() } returns flowOf(aClubMember())
            every { getNotifications("club1") } returns flowOf(emptyList())
            every { getUnreadCount("club1") } returns flowOf(0)

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(0, viewModel.unreadCount.value)
        }

    @Test
    fun `markAsRead calls use case with correct args`() =
        runTest {
            every { getUserClubMembership() } returns flowOf(aClubMember())
            every { getNotifications("club1") } returns flowOf(emptyList())
            every { getUnreadCount("club1") } returns flowOf(0)

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.markAsRead("notif1")
            advanceUntilIdle()

            coVerify { markAsRead("club1", "notif1") }
        }

    @Test
    fun `markAsUnread calls use case with correct args`() =
        runTest {
            every { getUserClubMembership() } returns flowOf(aClubMember())
            every { getNotifications("club1") } returns flowOf(emptyList())
            every { getUnreadCount("club1") } returns flowOf(0)

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.markAsUnread("notif1")
            advanceUntilIdle()

            coVerify { markAsUnread("club1", "notif1") }
        }

    @Test
    fun `deleteNotification calls use case with correct args`() =
        runTest {
            every { getUserClubMembership() } returns flowOf(aClubMember())
            every { getNotifications("club1") } returns flowOf(emptyList())
            every { getUnreadCount("club1") } returns flowOf(0)

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.deleteNotification("notif1")
            advanceUntilIdle()

            coVerify { deleteNotification("club1", "notif1") }
        }
}