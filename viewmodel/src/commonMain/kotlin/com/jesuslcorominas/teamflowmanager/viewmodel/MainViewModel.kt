package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasNotificationPermissionBeenRequestedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetNotificationPermissionRequestedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    private val hasNotificationPermissionBeenRequestedUseCase: HasNotificationPermissionBeenRequestedUseCase,
    private val setNotificationPermissionRequestedUseCase: SetNotificationPermissionRequestedUseCase,
    @Suppress("UNUSED_PARAMETER") getUserClubMembership: GetUserClubMembershipUseCase, // CLUB_HIDDEN
) : ViewModel() {
    // ==============================================================
    // CLUB_HIDDEN — Restaurar bloque CLUB_ORIGINAL para re-habilitar
    // la funcionalidad de club. Eliminar este bloque al revertir.
    // También: quitar @Suppress y el comentario "// CLUB_HIDDEN" del
    // parámetro getUserClubMembership de arriba.
    // ==============================================================
    val isPresident: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    // ==============================================================
    // FIN CLUB_HIDDEN
    // ==============================================================

    // ==============================================================
    // CLUB_ORIGINAL — Descomentar este bloque al revertir el cambio.
    // Eliminar el bloque CLUB_HIDDEN de arriba al revertir.
    // ==============================================================
//    val isPresident: StateFlow<Boolean> = getUserClubMembership()
//        .map { clubMember ->
//            clubMember?.hasRole(ClubRole.PRESIDENT) ?: false
//        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = false
//        )
    // ==============================================================
    // FIN CLUB_ORIGINAL
    // ==============================================================

    fun hasNotificationPermissionBeenRequested(): Boolean = hasNotificationPermissionBeenRequestedUseCase()

    fun setNotificationPermissionRequested(requested: Boolean) {
        setNotificationPermissionRequestedUseCase(requested)
    }
}
