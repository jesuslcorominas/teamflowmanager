package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FcmNotificationRepositoryImplTest {
    private lateinit var fcmDataSource: FcmDataSource
    private lateinit var repository: FcmNotificationRepositoryImpl

    @Before
    fun setup() {
        fcmDataSource = mockk(relaxed = true)
        repository = FcmNotificationRepositoryImpl(fcmDataSource)
    }

    @Test
    fun `givenUserHasMultipleTokens_whenSendNotificationToUser_thenSendsToEachToken`() = runTest {
        val payload = NotificationPayload.FreeText(title = "T", body = "B")
        coEvery { fcmDataSource.getTokensByUserId("user1") } returns listOf("token_a", "token_b", "token_c")

        repository.sendNotificationToUser("user1", payload)

        coVerify(exactly = 3) { fcmDataSource.sendNotification(any(), payload) }
    }

    @Test
    fun `givenUserHasNoTokens_whenSendNotificationToUser_thenNoNotificationSent`() = runTest {
        coEvery { fcmDataSource.getTokensByUserId(any()) } returns emptyList()

        repository.sendNotificationToUser("user1", NotificationPayload.FreeText("T", "B"))

        coVerify(exactly = 0) { fcmDataSource.sendNotification(any(), any()) }
    }

    @Test
    fun `givenTypedPayload_whenSendNotificationToUser_thenPayloadIsPassedThrough`() = runTest {
        val payload = NotificationPayload.Typed.AssignedAsCoach(teamName = "Team A")
        coEvery { fcmDataSource.getTokensByUserId("user1") } returns listOf("tok")

        repository.sendNotificationToUser("user1", payload)

        coVerify(exactly = 1) { fcmDataSource.sendNotification(token = "tok", payload = payload) }
    }

    @Test
    fun `givenUserHasTokens_whenSendNotificationToUser_thenQueriesCorrectUserId`() = runTest {
        coEvery { fcmDataSource.getTokensByUserId("user42") } returns listOf("tok")

        repository.sendNotificationToUser("user42", NotificationPayload.FreeText("T", "B"))

        coVerify { fcmDataSource.getTokensByUserId("user42") }
    }
}
