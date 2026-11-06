package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountDeletionDataSourceImplTest {
    private lateinit var authDataSource: AuthDataSource

    @Before
    fun setUp() {
        authDataSource = mockk(relaxed = true)
    }

    private fun dataSourceWith(
        baseUrl: String = "https://europe-west1-tfm.cloudfunctions.net",
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): Pair<AccountDeletionDataSourceImpl, MockEngine> {
        val engine = MockEngine { request -> handler(request) }
        val httpClient =
            HttpClient(engine) {
                install(HttpTimeout)
            }
        return AccountDeletionDataSourceImpl(httpClient, baseUrl, authDataSource) to engine
    }

    @Test
    fun `givenSuccessfulResponse_whenDeleteAccount_thenReturnsSuccess`() =
        runTest {
            // Given
            coEvery { authDataSource.getIdToken() } returns "id-token-123"
            val (dataSource, engine) = dataSourceWith { respond("") }

            // When
            val result = dataSource.deleteAccount()

            // Then
            assertTrue(result.isSuccess)
            val request = engine.requestHistory.single()
            assertEquals(HttpMethod.Post, request.method)
            assertEquals(
                "https://europe-west1-tfm.cloudfunctions.net/deleteAccount",
                request.url.toString(),
            )
            assertEquals("Bearer id-token-123", request.headers[HttpHeaders.Authorization])
            coVerify(exactly = 1) { authDataSource.getIdToken() }
        }

    @Test
    fun `givenBaseUrlWithTrailingSlash_whenDeleteAccount_thenCallsSingleSlashPath`() =
        runTest {
            // Given
            coEvery { authDataSource.getIdToken() } returns "id-token-123"
            val (dataSource, engine) =
                dataSourceWith(baseUrl = "https://europe-west1-tfm.cloudfunctions.net/") {
                    respond("")
                }

            // When
            val result = dataSource.deleteAccount()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(
                "https://europe-west1-tfm.cloudfunctions.net/deleteAccount",
                engine.requestHistory.single().url.toString(),
            )
        }

    @Test
    fun `givenNoSignedInUser_whenDeleteAccount_thenReturnsFailureWithoutCallingFunction`() =
        runTest {
            // Given
            coEvery { authDataSource.getIdToken() } returns null
            val (dataSource, engine) = dataSourceWith { respond("") }

            // When
            val result = dataSource.deleteAccount()

            // Then
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is IllegalStateException)
            assertEquals("No signed-in user to delete", error?.message)
            assertTrue(engine.requestHistory.isEmpty())
            coVerify(exactly = 1) { authDataSource.getIdToken() }
        }

    @Test
    fun `givenServerError_whenDeleteAccount_thenReturnsFailureWithStatusCode`() =
        runTest {
            // Given
            coEvery { authDataSource.getIdToken() } returns "id-token-123"
            val (dataSource, _) =
                dataSourceWith { respondError(HttpStatusCode.InternalServerError) }

            // When
            val result = dataSource.deleteAccount()

            // Then
            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is IllegalStateException)
            assertEquals("deleteAccount returned 500", error?.message)
        }

    @Test
    fun `givenUnauthorizedResponse_whenDeleteAccount_thenReturnsFailure`() =
        runTest {
            // Given
            coEvery { authDataSource.getIdToken() } returns "stale-token"
            val (dataSource, _) = dataSourceWith { respondError(HttpStatusCode.Unauthorized) }

            // When
            val result = dataSource.deleteAccount()

            // Then
            assertTrue(result.isFailure)
            assertEquals("deleteAccount returned 401", result.exceptionOrNull()?.message)
        }

    @Test
    fun `givenNetworkFailure_whenDeleteAccount_thenReturnsFailure`() =
        runTest {
            // Given
            coEvery { authDataSource.getIdToken() } returns "id-token-123"
            val (dataSource, _) = dataSourceWith { throw RuntimeException("connection reset") }

            // When
            val result = dataSource.deleteAccount()

            // Then
            assertTrue(result.isFailure)
            assertNotNull(result.exceptionOrNull())
        }

    @Test
    fun `givenTokenLookupFails_whenDeleteAccount_thenReturnsFailure`() =
        runTest {
            // Given
            coEvery { authDataSource.getIdToken() } throws RuntimeException("token refresh failed")
            val (dataSource, engine) = dataSourceWith { respond("") }

            // When
            val result = dataSource.deleteAccount()

            // Then
            assertTrue(result.isFailure)
            assertEquals("token refresh failed", result.exceptionOrNull()?.message)
            assertTrue(engine.requestHistory.isEmpty())
        }

    @Test
    fun `givenCancellation_whenDeleteAccount_thenRethrows`() =
        runTest {
            // Given
            coEvery { authDataSource.getIdToken() } throws CancellationException("cancelled")
            val (dataSource, _) = dataSourceWith { respond("") }

            // When
            var thrown: CancellationException? = null
            try {
                dataSource.deleteAccount()
            } catch (e: CancellationException) {
                thrown = e
            }

            // Then
            assertNotNull(thrown)
            assertEquals("cancelled", thrown?.message)
        }
}
