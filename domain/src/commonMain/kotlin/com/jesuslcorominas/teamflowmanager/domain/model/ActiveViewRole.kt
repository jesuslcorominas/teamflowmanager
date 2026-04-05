package com.jesuslcorominas.teamflowmanager.domain.model

sealed interface ActiveViewRole {
    data object President : ActiveViewRole

    data object Coach : ActiveViewRole
}
