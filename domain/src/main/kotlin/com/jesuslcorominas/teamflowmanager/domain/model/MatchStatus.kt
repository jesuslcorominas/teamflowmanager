package com.jesuslcorominas.teamflowmanager.domain.model

enum class MatchStatus {
    SCHEDULED,  // Scheduled
    IN_PROGRESS,    // In progress
    PAUSED,    // Half time
    TIMEOUT,   // Timeout (dead time)
    FINISHED   // Finished
}
