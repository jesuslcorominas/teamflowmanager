package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenProviderRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationTopicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteFcmTokenUseCaseTest {

    private lateinit var fcmTokenProviderRepository: FcmTokenProviderRepository
    private lateinit var fcmTokenRepository: FcmTokenRepository
    private lateinit var notificationTopicRepository: NotificationTopicRepository
    private lateinit var useCase: DeleteFcmTokenUseCaseImpl

    @Before
    fun setup() {
        fcmTokenProviderRepository = mockk(relaxed = true)
        fcmTokenRepository = mockk(relaxed = true)
        notificationTopicRepository = mockk(relaxed = true)
        useCase = DeleteFcmTokenUseCaseImpl(
            fcmTokenProviderRepository,
            fcmTokenRepository,
            notificationTopicRepository,
        )
    }

    @Test
    fun `when token is empty, does nothing`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns ""

        useCase("userA")

        coVerify(exactly = 0) { fcmTokenRepository.getTokenEntry(any(), any()) }
        coVerify(exactly = 0) { fcmTokenRepository.deleteToken(any(), any()) }
        coVerify(exactly = 0) { notificationTopicRepository.unsubscribeFromRawTopic(any()) }
    }

    @Test
    fun `when token entry has topic, unsubscribes before deleting`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns "token123"
        coEvery { fcmTokenRepository.getTokenEntry("userA", "token123") } returns
            FcmTokenEntry("docId", "userA", "club_clubX")

        useCase("userA")

        coVerifyOrder {
            notificationTopicRepository.unsubscribeFromRawTopic("club_clubX")
            fcmTokenRepository.deleteToken("userA", "token123")
        }
    }

    @Test
    fun `when token entry has no topic, deletes without unsubscribing`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns "token123"
        coEvery { fcmTokenRepository.getTokenEntry("userA", "token123") } returns
            FcmTokenEntry("docId", "userA", null)

        useCase("userA")

        coVerify(exactly = 0) { notificationTopicRepository.unsubscribeFromRawTopic(any()) }
        coVerify { fcmTokenRepository.deleteToken("userA", "token123") }
    }

    @Test
    fun `when token entry does not exist in firestore, still deletes token`() = runTest {
        coEvery { fcmTokenProviderRepository.getToken() } returns "token123"
        coEvery { fcmTokenRepository.getTokenEntry("userA", "token123") } returns null

        useCase("userA")

        coVerify(exactly = 0) { notificationTopicRepository.unsubscribeFromRawTopic(any()) }
        coVerify { fcmTokenRepository.deleteToken("userA", "token123") }
    }
}
