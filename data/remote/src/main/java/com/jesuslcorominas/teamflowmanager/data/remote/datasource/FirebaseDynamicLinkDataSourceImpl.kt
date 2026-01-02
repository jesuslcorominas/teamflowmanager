package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.net.Uri
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.jesuslcorominas.teamflowmanager.data.core.datasource.DynamicLinkDataSource
import kotlinx.coroutines.tasks.await

/**
 * Firebase Dynamic Links implementation for generating shareable team invitation links.
 * 
 * Generates short, clickable URLs that:
 * - Work in all messaging apps (WhatsApp, Email, SMS)
 * - Redirect to Play Store if app not installed
 * - Open the app with deep link if installed
 * - Support social sharing with rich meta tags
 */
internal class FirebaseDynamicLinkDataSourceImpl : DynamicLinkDataSource {

    companion object {
        // Firebase Dynamic Links domain (provided by Firebase Console)
        private const val DYNAMIC_LINK_DOMAIN = "https://teamflowmanager.page.link"
        
        // Deep link URL that the app will open
        private const val DEEP_LINK_BASE = "https://teamflowmanager.app/team/accept"
        
        // Android package name
        private const val ANDROID_PACKAGE_NAME = "com.jesuslcorominas.teamflowmanager"
        
        // Play Store link (fallback if app not installed)
        private const val PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=$ANDROID_PACKAGE_NAME"
        
        // Fallback custom scheme if Firebase Dynamic Links fails
        private const val FALLBACK_SCHEME = "teamflowmanager://team/accept"
    }

    override suspend fun generateTeamInvitationLink(
        teamFirestoreId: String,
        teamName: String
    ): String {
        // Build deep link URI with parameters
        val deepLinkUri = Uri.parse(DEEP_LINK_BASE)
            .buildUpon()
            .appendQueryParameter("teamId", teamFirestoreId)
            .appendQueryParameter("teamName", teamName)
            .build()

        // Create Firebase Dynamic Link
        return try {
            val dynamicLink = Firebase.dynamicLinks.shortLinkAsync {
                link = deepLinkUri
                domainUriPrefix = DYNAMIC_LINK_DOMAIN
                
                // Android parameters
                androidParameters(ANDROID_PACKAGE_NAME) {
                    // Minimum app version (optional)
                    minimumVersion = 1
                    
                    // Fallback URL if app not installed (Play Store)
                    fallbackUrl = Uri.parse(PLAY_STORE_LINK)
                }
                
                // Social meta tags for better sharing experience
                socialMetaTagParameters {
                    title = "Únete a $teamName"
                    description = "Has sido invitado a ser el entrenador de $teamName en Team Flow Manager"
                }
            }.await()
            
            // Return the short link
            dynamicLink.shortLink.toString()
        } catch (e: Exception) {
            // If Dynamic Links fails, fall back to custom scheme
            // This can happen if Firebase Dynamic Links is not properly configured
            "$FALLBACK_SCHEME?teamId=$teamFirestoreId&teamName=${teamName.replace(" ", "%20")}"
        }
    }
}
