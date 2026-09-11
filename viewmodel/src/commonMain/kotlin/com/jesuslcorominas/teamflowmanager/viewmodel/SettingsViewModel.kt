package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetNotificationPreferencesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateGlobalNotificationPreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteFcmTokenUseCase: DeleteFcmTokenUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val getTeam: GetTeamUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val observeActiveViewRole: ObserveActiveViewRoleUseCase,
    private val setActiveViewRole: SetActiveViewRoleUseCase,
    private val getNotificationPreferences: GetNotificationPreferencesUseCase,
    private val updateGlobalNotificationPreference: UpdateGlobalNotificationPreferenceUseCase,
    private val observeSubstitutionMode: ObserveSubstitutionModeUseCase,
    private val setSubstitutionMode: SetSubstitutionModeUseCase,
    private val getAllMatches: GetAllMatchesUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    val currentUser: StateFlow<User?> =
        getCurrentUserUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _signOutComplete = MutableStateFlow(false)
    val signOutComplete: StateFlow<Boolean> = _signOutComplete.asStateFlow()

    private val _roleSelectorState = MutableStateFlow(RoleSelectorState())
    val roleSelectorState: StateFlow<RoleSelectorState> = _roleSelectorState.asStateFlow()

    data class NotificationPreferencesState(
        val matchEventsState: GlobalNotificationState = GlobalNotificationState.ALL_ON,
        val goalsState: GlobalNotificationState = GlobalNotificationState.ALL_ON,
        val clubId: String = "",
    )

    private val _notificationPreferences = MutableStateFlow(NotificationPreferencesState())
    val notificationPreferences: StateFlow<NotificationPreferencesState> = _notificationPreferences.asStateFlow()

    /**
     * Set when saving a notification preference failed, so the screen can tell the user the switch
     * did not stick. The switch itself needs no reverting: it renders from
     * [notificationPreferences], which only changes once Firestore confirms the write.
     */
    private val _notificationUpdateFailed = MutableStateFlow(false)
    val notificationUpdateFailed: StateFlow<Boolean> = _notificationUpdateFailed.asStateFlow()

    data class RoleSelectorState(
        val showRoleSelector: Boolean = false,
        /** False when the president has no team assigned: there is no coach view to switch to. */
        val isRoleSelectorEnabled: Boolean = false,
        /** The role currently in effect for the rest of the app. */
        val activeRole: ActiveViewRole = ActiveViewRole.President,
        /** What the switch shows. Applied on leaving the screen, not on every tap. */
        val selectedRole: ActiveViewRole = ActiveViewRole.President,
    )

    /** The running match that locks the setting, named so the user knows what to go and finish. */
    data class BlockingMatch(
        val opponent: String,
        val dateTime: Long?,
    )

    data class SubstitutionModeState(
        val mode: SubstitutionMode = SubstitutionMode.SCHEDULED,
        /**
         * Non-null while a match is running: switching mode mid-match would strand queued changes.
         *
         * A match only leaves IN_PROGRESS/PAUSED through an explicit finish, and nothing recovers
         * abandoned ones, so a single match left open by a killed app would otherwise grey the
         * switch out forever with no clue as to why. Naming the match gives the user somewhere
         * to go.
         */
        val blockingMatch: BlockingMatch? = null,
    ) {
        val isEnabled: Boolean get() = blockingMatch == null
    }

    /**
     * The switch renders from the persisted value, and locks while a match of the current team is
     * running.
     *
     * Shared with [SharingStarted.WhileSubscribed] like the other flows here, rather than collected
     * for the ViewModel's whole life: [getAllMatches] opens a Firestore listener, and it has no
     * business staying open while Settings is off screen just to decide whether a switch is grey.
     *
     * [getAllMatches] can also fail, and an uncaught throw in [viewModelScope] kills the process.
     * Catching degrades to "enabled", the deliberate safe side — locking the user out of a
     * device-local setting because a query failed is worse than letting them change it. The catch
     * ends the match flow, so it does not recover within one subscription; reopening Settings
     * resubscribes and retries.
     */
    val substitutionModeState: StateFlow<SubstitutionModeState> =
        combine(
            observeSubstitutionMode(),
            getAllMatches()
                .map { matches ->
                    matches.firstOrNull {
                        it.status == MatchStatus.IN_PROGRESS || it.status == MatchStatus.PAUSED
                    }
                }
                .catch { emit(null) },
        ) { mode, runningMatch ->
            SubstitutionModeState(
                mode = mode,
                blockingMatch = runningMatch?.let { BlockingMatch(it.opponent, it.dateTime) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubstitutionModeState())

    init {
        loadRoleSelectorState()
    }

    /**
     * Applied on the spot, unlike the role selector: nothing else in the app shell reacts to this
     * setting mid-session, so there is no reason to defer it to screen exit.
     */
    fun onSubstitutionModeChanged(scheduled: Boolean) {
        setSubstitutionMode(if (scheduled) SubstitutionMode.SCHEDULED else SubstitutionMode.LIVE)
    }

    private fun loadRoleSelectorState() {
        viewModelScope.launch {
            val clubMember = getUserClubMembership().first() ?: return@launch
            val isPresident = clubMember.hasRole(ClubRole.PRESIDENT)

            if (isPresident) {
                val team = getTeam().first()
                launch {
                    observeActiveViewRole().collect { role ->
                        // Only emits once a selection has been committed, so resetting the pending
                        // selection here cannot discard an in-progress one.
                        _roleSelectorState.value =
                            RoleSelectorState(
                                showRoleSelector = true,
                                isRoleSelectorEnabled = team != null,
                                activeRole = role,
                                selectedRole = role,
                            )
                    }
                }

                val clubRemoteId = clubMember.clubId.takeIf { it.isNotBlank() } ?: return@launch

                getNotificationPreferences(clubRemoteId).collect { prefs ->
                    _notificationPreferences.value =
                        NotificationPreferencesState(
                            matchEventsState = prefs.globalStateFor(NotificationEventType.MATCH_EVENTS),
                            goalsState = prefs.globalStateFor(NotificationEventType.GOALS),
                            clubId = clubRemoteId,
                        )
                }
            }
        }
    }

    fun updateGlobalMatchEvents(enabled: Boolean) {
        updateGlobalPreference(NotificationEventType.MATCH_EVENTS, enabled)
    }

    fun updateGlobalGoals(enabled: Boolean) {
        updateGlobalPreference(NotificationEventType.GOALS, enabled)
    }

    /**
     * The data source rethrows on failure and nothing above it used to catch, so any Firestore
     * error — offline, permission denied, timeout — reached the default handler through
     * [viewModelScope] and killed the process. Report it and surface it instead.
     */
    private fun updateGlobalPreference(
        type: NotificationEventType,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            runCatching {
                updateGlobalNotificationPreference(_notificationPreferences.value.clubId, type, enabled)
            }.onFailure { error ->
                crashReporter.recordException(error)
                _notificationUpdateFailed.value = true
            }
        }
    }

    fun onNotificationUpdateErrorShown() {
        _notificationUpdateFailed.value = false
    }

    /**
     * Records what the switch shows, without applying it.
     *
     * Applying on every tap made the bottom bar change under the user while they were still in
     * Settings, and moved them to another screen mid-interaction. The selection is committed by
     * [commitRoleSelection] when the screen goes away, so the role can be toggled any number of
     * times and only the final choice counts.
     */
    fun onRoleSelected(role: ActiveViewRole) {
        _roleSelectorState.value = _roleSelectorState.value.copy(selectedRole = role)
    }

    /**
     * Applies the pending selection, if it differs from the role in effect. Called when the screen
     * is left; persisting is what makes the shell react, so the navigation happens on the way out.
     */
    fun commitRoleSelection() {
        val state = _roleSelectorState.value
        if (state.showRoleSelector && state.selectedRole != state.activeRole) {
            setActiveViewRole(state.selectedRole)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                runCatching { deleteFcmTokenUseCase(user.id) }
            }
            signOutUseCase()
            analyticsTracker.logEvent("logout", emptyMap())
            analyticsTracker.setUserId(null)
            _signOutComplete.value = true
        }
    }

    fun clearSignOutComplete() {
        _signOutComplete.value = false
    }
}
