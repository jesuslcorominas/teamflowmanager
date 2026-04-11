package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenProviderRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationTopicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SyncFcmTokenUseCaseTest {

    private lateinit var fcmTokenProviderRepository: FcmTokenProviderRepository
    private lateinit var fcmTokenRepository: FcmTokenRepository
    private lateinit var notificationTopicRepository: NotificationTopicRepository
    private lateinit var useCase: SyncFcmTokenUseCaseImpl

    @Before
    fun setup() {
        fcmTokenProviderRepository = mockk(relaxed = true)
        fcmTokenRepository = mockk(relaxed = true)
        notificationTopicRepository = mockk(relaxed = true)
        useCase = SyncFcmTokenUseCaseImpl(
            fcmTokenProviderRepository,
            fcmTokenRepository,
            notificationTopicRepository,
        )
    }

    @Test
    fun `when token is empty, does nothing`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns ""

        useCase("userA", "android", "club1")

        coVerify(exactly = 0) { fcmTokenRepository.saveToken(any(), any(), any(), any()) }
        coVerify(exactly = 0) { fcmTokenRepository.deleteToken(any(), any()) }
        coVerify(exactly = 0) { notificationTopicRepository.subscribeToClub(any()) }
        coVerify(exactly = 0) { notificationTopicRepository.unsubscribeFromRawTopic(any()) }
    }

    @Test
    fun `when no other users on device, saves token and subscribes to club`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns "token123"
        coEvery { fcmTokenRepository.findTokensForOtherUsers("token123", "userA") } returns emptyList()

        useCase("userA", "android", "club1")

        coVerify { fcmTokenRepository.saveToken("userA", "token123", "android", "club_club1") }
        coVerify { notificationTopicRepository.subscribeToClub("club1") }
        coVerify(exactly = 0) { notificationTopicRepository.unsubscribeFromRawTopic(any()) }
        coVerify(exactly = 0) { fcmTokenRepository.deleteToken(any(), any()) }
    }

    @Test
    fun `when another user token exists, unsubscribes and cleans up before saving`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns "token123"
        coEvery { fcmTokenRepository.findTokensForOtherUsers("token123", "userB") } returns listOf(
            FcmTokenEntry("docId1", "userA", "club_clubA"),
        )

        useCase("userB", "android", "club2")

        coVerify { notificationTopicRepository.unsubscribeFromRawTopic("club_clubA") }
        coVerify { fcmTokenRepository.deleteToken("userA", "token123") }
        coVerify { fcmTokenRepository.saveToken("userB", "token123", "android", "club_club2") }
        coVerify { notificationTopicRepository.subscribeToClub("club2") }
    }

    @Test
    fun `when clubRemoteId is null, saves token without subscribing`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns "token123"
        coEvery { fcmTokenRepository.findTokensForOtherUsers(any(), any()) } returns emptyList()

        useCase("userA", "android", null)

        coVerify { fcmTokenRepository.saveToken("userA", "token123", "android", null) }
        coVerify(exactly = 0) { notificationTopicRepository.subscribeToClub(any()) }
    }

    @Test
    fun `when other user has no topic, skips unsubscribe but still deletes`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns "token123"
        coEvery { fcmTokenRepository.findTokensForOtherUsers("token123", "userB") } returns listOf(
            FcmTokenEntry("docId1", "userA", null),
        )

        useCase("userB", "android", null)

        coVerify(exactly = 0) { notificationTopicRepository.unsubscribeFromRawTopic(any()) }
        coVerify { fcmTokenRepository.deleteToken("userA", "token123") }
    }
}
