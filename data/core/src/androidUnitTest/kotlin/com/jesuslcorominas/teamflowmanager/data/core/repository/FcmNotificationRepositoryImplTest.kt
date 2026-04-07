package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmDataSource
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
    fun `given UserHasMultipleTokens when SendNotificationToUser then SendsToEachToken`() = runTest {
        coEvery { fcmDataSource.getTokensByUserId("user1") } returns listOf("token_a", "token_b", "token_c")

        repository.sendNotificationToUser("user1", "Title", "Body")

        coVerify(exactly = 3) { fcmDataSource.sendNotification(any(), "Title", "Body") }
    }

    @Test
    fun `given UserHasNoTokens when SendNotificationToUser then NoNotificationSent`() = runTest {
        coEvery { fcmDataSource.getTokensByUserId(any()) } returns emptyList()

        repository.sendNotificationToUser("user1", "Title", "Body")

        coVerify(exactly = 0) { fcmDataSource.sendNotification(any(), any(), any()) }
    }

    @Test
    fun `givenUserHasSingleToken_whenSendNotificationToUser_thenSendsCorrectTitleAndBody`() = runTest {
        coEvery { fcmDataSource.getTokensByUserId("user1") } returns listOf("token_x")

        repository.sendNotificationToUser("user1", "Mi título", "Mi cuerpo")

        coVerify(exactly = 1) {
            fcmDataSource.sendNotification(token = "token_x", title = "Mi título", body = "Mi cuerpo")
        }
    }

    @Test
    fun `givenUserHasTokens_whenSendNotificationToUser_thenQueriesCorrectUserId`() = runTest {
        coEvery { fcmDataSource.getTokensByUserId("user42") } returns listOf("tok")

        repository.sendNotificationToUser("user42", "T", "B")

        coVerify { fcmDataSource.getTokensByUserId("user42") }
    }
}
