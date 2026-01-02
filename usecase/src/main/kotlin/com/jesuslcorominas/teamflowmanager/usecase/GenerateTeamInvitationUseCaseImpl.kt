package com.jesuslcorominas.teamflowmanager.usecase

import android.net.Uri
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

internal class GenerateTeamInvitationUseCaseImpl(
    private val teamRepository: TeamRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : GenerateTeamInvitationUseCase {

    companion object {
        // Firebase Dynamic Links domain (provided by Firebase Console)
        // This needs to be configured in Firebase Console first
        private const val DYNAMIC_LINK_DOMAIN = "https://teamflowmanager.page.link"
        
        // Deep link URL that the app will open
        private const val DEEP_LINK_BASE = "https://teamflowmanager.app/team/accept"
        
        // Android package name
        private const val ANDROID_PACKAGE_NAME = "com.jesuslcorominas.teamflowmanager"
        
        // Play Store link (fallback if app not installed)
        private const val PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=$ANDROID_PACKAGE_NAME"
    }

    override suspend fun invoke(teamFirestoreId: String, teamName: String): String {
        // Validate inputs
        require(teamFirestoreId.isNotBlank()) {
            "Team Firestore ID cannot be blank"
        }

        // Get current authenticated user
        val currentUser = getCurrentUser().first()
            ?: throw IllegalStateException("User must be authenticated to generate team invitation")

        // Get the team
        val team = teamRepository.getTeamByFirestoreId(teamFirestoreId)
            ?: throw IllegalArgumentException("Team not found with Firestore ID: $teamFirestoreId")

        // Verify team belongs to a club
        require(team.clubFirestoreId != null) {
            "Team must belong to a club to generate invitation"
        }

        // Get current user's club membership
        val currentUserMembership = clubMemberRepository.getClubMemberByUserId(currentUser.id).first()
            ?: throw IllegalStateException("User must be a club member to generate team invitation")

        // Verify current user is a President
        require(currentUserMembership.role == ClubRole.PRESIDENT.roleName) {
            "Only club Presidents can generate team invitations"
        }

        // Verify they are in the same club
        require(currentUserMembership.clubFirestoreId == team.clubFirestoreId) {
            "User and team must be in the same club"
        }

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
                
                // iOS parameters (optional for future)
                // iosParameters("com.jesuslcorominas.teamflowmanager.ios") {
                //     appStoreId = "your-app-store-id"
                //     minimumVersion = "1.0"
                // }
                
                // Social meta tags for better sharing experience
                socialMetaTagParameters {
                    title = "Únete a $teamName"
                    description = "Has sido invitado a ser el entrenador de $teamName en Team Flow Manager"
                    // Optional: Add an image URL for social sharing
                    // imageUrl = Uri.parse("https://your-image-url.com/team-icon.png")
                }
            }.await()
            
            // Return the short link
            dynamicLink.shortLink.toString()
        } catch (e: Exception) {
            // If Dynamic Links fails, fall back to custom scheme (less ideal but works)
            // This can happen if Firebase Dynamic Links is not properly configured
            "teamflowmanager://team/accept?teamId=$teamFirestoreId&teamName=${teamName.replace(" ", "%20")}"
        }
    }
}
