package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FcmNotificationRepositoryImplTest {
    private lateinit var fcmTokenDataSource: FcmTokenDataSource
    private lateinit var repository: FcmNotificationRepositoryImpl

    @Before
    fun setup() {
        fcmTokenDataSource = mockk(relaxed = true)
        repository = FcmNotificationRepositoryImpl(fcmTokenDataSource)
    }

    @Test
    fun `givenUserHasMultipleTokens_whenSendNotificationToUser_thenSendsToEachToken`() = runTest {
        coEvery { fcmTokenDataSource.getTokensByUserId("user1") } returns listOf("token_a", "token_b", "token_c")

        repository.sendNotificationToUser("user1", "Title", "Body")

        coVerify(exactly = 3) { fcmTokenDataSource.sendNotification(any(), "Title", "Body") }
    }

    @Test
    fun `givenUserHasNoTokens_whenSendNotificationToUser_thenNoNotificationSent`() = runTest {
        coEvery { fcmTokenDataSource.getTokensByUserId(any()) } returns emptyList()

        repository.sendNotificationToUser("user1", "Title", "Body")

        coVerify(exactly = 0) { fcmTokenDataSource.sendNotification(any(), any(), any()) }
    }

    @Test
    fun `givenUserHasSingleToken_whenSendNotificationToUser_thenSendsCorrectTitleAndBody`() = runTest {
        coEvery { fcmTokenDataSource.getTokensByUserId("user1") } returns listOf("token_x")

        repository.sendNotificationToUser("user1", "Mi título", "Mi cuerpo")

        coVerify(exactly = 1) {
            fcmTokenDataSource.sendNotification(token = "token_x", title = "Mi título", body = "Mi cuerpo")
        }
    }

    @Test
    fun `givenUserHasTokens_whenSendNotificationToUser_thenQueriesCorrectUserId`() = runTest {
        coEvery { fcmTokenDataSource.getTokensByUserId("user42") } returns listOf("tok")

        repository.sendNotificationToUser("user42", "T", "B")

        coVerify { fcmTokenDataSource.getTokensByUserId("user42") }
    }
}
