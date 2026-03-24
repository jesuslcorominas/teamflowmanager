package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.jesuslcorominas.teamflowmanager.data.remote.api.ShortLinkApi
import com.jesuslcorominas.teamflowmanager.data.remote.api.model.CreateShortLinkRequest
import com.jesuslcorominas.teamflowmanager.data.remote.api.model.CreateShortLinkResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirebaseDynamicLinkDataSourceImplTest {

    private val mockShortLinkApi = mockk<ShortLinkApi>()
    private lateinit var dataSource: FirebaseDynamicLinkDataSourceImpl

    @After
    fun tearDown() {
        unmockkAll()
    }

        @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.i(any(), any()) } returns 0

        dataSource = FirebaseDynamicLinkDataSourceImpl(mockShortLinkApi)
    }

    @Test
    fun `givenApiReturnsShortLink_whenGenerateTeamInvitationLink_thenReturnsShortLink`() = runTest {
        val expectedLink = "https://short.link/abc"
        coEvery { mockShortLinkApi.createShortLink(any()) } returns CreateShortLinkResponse(shortLink = expectedLink)

        val result = dataSource.generateTeamInvitationLink("team-123", "Test Team")

        assertEquals(expectedLink, result)
    }

    @Test
    fun `givenApiThrowsException_whenGenerateTeamInvitationLink_thenReturnsFallbackLink`() = runTest {
        coEvery { mockShortLinkApi.createShortLink(any()) } throws RuntimeException("Network error")

        val result = dataSource.generateTeamInvitationLink("team-123", "TestTeam")

        assertTrue(result.startsWith("teamflowmanager://team/accept"))
    }

    @Test
    fun `givenTeamNameWithSpaces_whenApiFails_thenFallbackLinkUrlEncodesTeamName`() = runTest {
        coEvery { mockShortLinkApi.createShortLink(any()) } throws RuntimeException("Network error")

        val result = dataSource.generateTeamInvitationLink("team-123", "Real Madrid")

        assertTrue(result.contains("Real+Madrid"))
    }

    @Test
    fun `givenTeamId_whenApiFails_thenFallbackLinkContainsTeamId`() = runTest {
        coEvery { mockShortLinkApi.createShortLink(any()) } throws RuntimeException("Network error")

        val result = dataSource.generateTeamInvitationLink("abc123", "SomeTeam")

        assertTrue(result.contains("teamId=abc123"))
    }
}
