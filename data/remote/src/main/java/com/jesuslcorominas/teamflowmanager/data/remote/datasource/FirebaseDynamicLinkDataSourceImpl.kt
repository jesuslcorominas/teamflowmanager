package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.api.ShortLinkApi
import com.jesuslcorominas.teamflowmanager.data.remote.api.model.CreateShortLinkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.URLEncoder

/**
 * Custom short link implementation using Firebase Cloud Functions + Hosting via Ktorfit.
 *
 * Replaces deprecated Firebase Dynamic Links with a custom solution that:
 * - Generates short, clickable URLs (https://teamflowmanager.web.app/l/xxxxx)
 * - Works in all messaging apps (WhatsApp, Email, SMS)
 * - Redirects to Play Store if app not installed
 * - Opens the app with deep link if installed
 * - Supports social sharing with Open Graph meta tags
 * - Completely serverless (Firebase Hosting + Cloud Functions)
 * - Uses Ktorfit for type-safe HTTP calls
 */
internal class FirebaseDynamicLinkDataSourceImpl(
    private val shortLinkApi: ShortLinkApi
) : DynamicLinkDataSource {

    companion object {
        private const val TAG = "FirebaseShortLink"

        // Fallback custom scheme if Cloud Function fails
        private const val FALLBACK_SCHEME = "teamflowmanager://team/accept"
        
        // Timeout for API call (5 seconds)
        private const val API_TIMEOUT_MS = 5_000L
    }

    override suspend fun generateTeamInvitationLink(
        teamFirestoreId: String,
        teamName: String
    ): String = withContext(Dispatchers.IO) {
        return@withContext try {
            // Call Cloud Function via Ktorfit to create short link
            val request = CreateShortLinkRequest(
                teamId = teamFirestoreId,
                teamName = teamName
            )

            Log.d(TAG, "Calling Cloud Function to create short link for team: $teamName")
            Log.d(TAG, "Request: teamId=$teamFirestoreId, teamName=$teamName")
            
            // Make the API call with explicit timeout
            val response = try {
                Log.d(TAG, "About to call shortLinkApi.createShortLink...")
                withTimeout(API_TIMEOUT_MS) {
                    Log.d(TAG, "Inside withTimeout block, calling API...")
                    val result = shortLinkApi.createShortLink(request)
                    Log.d(TAG, "API call completed, got result")
                    result
                }
            } catch (e: Exception) {
                Log.e(TAG, "!!! EXCEPTION CAUGHT !!!")
                Log.e(TAG, "Exception while calling shortLinkApi.createShortLink", e)
                Log.e(TAG, "Exception type: ${e.javaClass.name}")
                Log.e(TAG, "Exception message: ${e.message}")
                Log.e(TAG, "Exception cause: ${e.cause}")
                Log.e(TAG, "Stack trace:")
                e.printStackTrace()
                throw e
            }
            
            Log.d(TAG, "Received response from Cloud Function")
            Log.d(TAG, "Response shortLink: ${response.shortLink}")
            Log.d(TAG, "Response linkId: ${response.linkId}")
            Log.d(TAG, "Successfully created short link: ${response.shortLink}")
            response.shortLink
        } catch (e: Exception) {
            // If any error occurs, fall back to custom scheme
            Log.e(TAG, "!!! OUTER EXCEPTION CAUGHT !!!")
            Log.e(TAG, "Error creating short link via Cloud Function", e)
            Log.e(TAG, "Error details: ${e.message}")
            Log.e(TAG, "Error type: ${e.javaClass.name}")
            Log.e(TAG, "Error cause: ${e.cause?.javaClass?.name}")
            Log.w(TAG, "Falling back to custom scheme (not clickable in WhatsApp)")
            
            createFallbackLink(teamFirestoreId, teamName)
        }
    }

    private fun createFallbackLink(teamFirestoreId: String, teamName: String): String {
        val encodedTeamName = URLEncoder.encode(teamName, "UTF-8")
        val fallbackLink = "$FALLBACK_SCHEME?teamId=$teamFirestoreId&teamName=$encodedTeamName"
        Log.d(TAG, "Created fallback link: $fallbackLink")
        return fallbackLink
    }
}
