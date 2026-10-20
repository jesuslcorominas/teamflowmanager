package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AccountDeletionDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.AuthDataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException

/**
 * Calls the `deleteAccount` Cloud Function, which runs the cascade with Admin
 * privileges. See docs/ACCOUNT_DELETION.md.
 *
 * Written against the raw Ktor client rather than the Ktorfit interfaces used
 * elsewhere in this module for two reasons: Ktorfit is generated for the Android
 * target only, and this one call needs a request timeout far longer than the
 * five seconds the shared client is configured with — a cascade over a club with
 * several seasons of matches takes a while.
 */
internal class AccountDeletionDataSourceImpl(
    private val httpClient: HttpClient,
    private val functionsBaseUrl: String,
    private val authDataSource: AuthDataSource,
) : AccountDeletionDataSource {
    companion object {
        private const val REQUEST_TIMEOUT_MS = 120_000L
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val idToken =
                authDataSource.getIdToken()
                    ?: return Result.failure(IllegalStateException("No signed-in user to delete"))

            val response: HttpResponse =
                httpClient.post("${functionsBaseUrl.trimEnd('/')}/deleteAccount") {
                    header(HttpHeaders.Authorization, "Bearer $idToken")
                    timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MS }
                }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                // Deliberately not retried: the cascade is not idempotent from the
                // client's point of view — a failure may have deleted part of the
                // data, and the user needs to be told it did not finish rather than
                // shown a success.
                Result.failure(
                    IllegalStateException("deleteAccount returned ${response.status.value}"),
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
