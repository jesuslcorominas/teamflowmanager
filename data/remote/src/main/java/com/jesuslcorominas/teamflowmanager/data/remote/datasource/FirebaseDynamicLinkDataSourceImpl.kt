package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Custom short link implementation using Firebase Cloud Functions + Hosting.
 * 
 * Replaces deprecated Firebase Dynamic Links with a custom solution that:
 * - Generates short, clickable URLs (https://teamflowmanager.web.app/l/xxxxx)
 * - Works in all messaging apps (WhatsApp, Email, SMS)
 * - Redirects to Play Store if app not installed
 * - Opens the app with deep link if installed
 * - Supports social sharing with Open Graph meta tags
 * - Completely serverless (Firebase Hosting + Cloud Functions)
 */
internal class FirebaseDynamicLinkDataSourceImpl : DynamicLinkDataSource {

    companion object {
        private const val TAG = "FirebaseShortLink"
        
        // Cloud Function endpoint (deployed via Firebase Hosting)
        private const val CLOUD_FUNCTION_URL = "https://teamflowmanager.web.app/api/createShortLink"
        
        // Fallback custom scheme if Cloud Function fails
        private const val FALLBACK_SCHEME = "teamflowmanager://team/accept"
    }

    override suspend fun generateTeamInvitationLink(
        teamFirestoreId: String,
        teamName: String
    ): String = withContext(Dispatchers.IO) {
        try {
            // Call Cloud Function to create short link
            val url = URL(CLOUD_FUNCTION_URL)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 10000
                readTimeout = 10000
            }

            // Build JSON payload
            val jsonPayload = JSONObject().apply {
                put("teamId", teamFirestoreId)
                put("teamName", teamName)
            }

            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonPayload.toString())
                writer.flush()
            }

            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val shortLink = jsonResponse.getString("shortLink")
                
                Log.d(TAG, "Successfully created short link: $shortLink")
                shortLink
            } else {
                // If request fails, fall back to custom scheme
                Log.w(TAG, "Failed to create short link, response code: $responseCode")
                createFallbackLink(teamFirestoreId, teamName)
            }
        } catch (e: Exception) {
            // If any error occurs, fall back to custom scheme
            Log.e(TAG, "Error creating short link", e)
            createFallbackLink(teamFirestoreId, teamName)
        }
    }

    private fun createFallbackLink(teamFirestoreId: String, teamName: String): String {
        val encodedTeamName = URLEncoder.encode(teamName, "UTF-8")
        return "$FALLBACK_SCHEME?teamId=$teamFirestoreId&teamName=$encodedTeamName"
    }
}
