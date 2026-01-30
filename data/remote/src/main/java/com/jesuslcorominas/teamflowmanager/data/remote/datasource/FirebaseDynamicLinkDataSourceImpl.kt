package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.api.ShortLinkApi
import com.jesuslcorominas.teamflowmanager.data.remote.api.model.CreateShortLinkRequest
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
    }

    override suspend fun generateTeamInvitationLink(
        teamFirestoreId: String,
        teamName: String
    ): String {
        return try {
            // Call Cloud Function via Ktorfit to create short link
            val request = CreateShortLinkRequest(
                teamId = teamFirestoreId,
                teamName = teamName
            )

            Log.d(TAG, "Calling Cloud Function to create short link for team: $teamName")
            
            // Make the API call - this is a suspend function that should complete or throw
            val response = try {
                shortLinkApi.createShortLink(request)
            } catch (e: Exception) {
                Log.e(TAG, "Exception while calling shortLinkApi.createShortLink", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
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
            Log.e(TAG, "Error creating short link via Cloud Function. This usually means the Cloud Functions are not deployed yet.", e)
            Log.e(TAG, "Error details: ${e.message}")
            Log.e(TAG, "Error type: ${e.javaClass.name}")
            Log.w(TAG, "Falling back to custom scheme (not clickable in WhatsApp)")
            
            createFallbackLink(teamFirestoreId, teamName)
        }
    }

    private fun createFallbackLink(teamFirestoreId: String, teamName: String): String {
        val encodedTeamName = URLEncoder.encode(teamName, "UTF-8")
        return "$FALLBACK_SCHEME?teamId=$teamFirestoreId&teamName=$encodedTeamName"
    }
}
