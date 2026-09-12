package com.jesuslcorominas.teamflowmanager.viewmodel

import org.koin.core.scope.Scope

/**
 * Builds a [MatchViewModel] with its collaborators from a Koin scope.
 *
 * It lives here, and not in the DI module, for a visibility reason: the collaborators are
 * `internal` to `:viewmodel`, so only code inside this module can name their types. `:di`
 * registers iOS ViewModels from a different Gradle module and could not construct them. This
 * keeps DI deciding *what* is registered while it stops needing to know *how* it is built.
 */
fun Scope.createMatchViewModel(matchId: String): MatchViewModel =
    MatchViewModel(
        matchId = matchId,
        timeTicker = get(),
        analyticsTracker = get(),
        crashReporter = get(),
        notifyPresidentMatchEvent = get(),
        getTeamUseCase = get(),
        clock =
            MatchClockController(
                startMatchTimerUseCase = get(),
                startPlayerTimersBatchUseCase = get(),
                synchronizeTimeUseCase = get(),
                pauseMatchUseCase = get(),
                resumeMatchUseCase = get(),
                finishMatchUseCase = get(),
                startTimeoutUseCase = get(),
                endTimeoutUseCase = get(),
                getMatchById = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            ),
        substitutions =
            MatchSubstitutionCoordinator(
                matchId = matchId,
                registerPlayerSubstitutionUseCase = get(),
                observeSubstitutionModeUseCase = get(),
                observePendingSubstitutionsUseCase = get(),
                getPendingSubstitutionConflictsUseCase = get(),
                addPendingSubstitutionUseCase = get(),
                removePendingSubstitutionUseCase = get(),
                clearPendingSubstitutionsUseCase = get(),
                shouldShowInvalidSubstitutionAlertUseCase = get(),
                setShouldShowInvalidSubstitutionAlertUseCase = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            ),
        stateLoader =
            MatchStateLoader(
                getMatchById = get(),
                getAllPlayerTimesUseCase = get(),
                getPlayersByTeamUseCase = get(),
                getMatchTimelineUseCase = get(),
                getMatchSummaryUseCase = get(),
            ),
        goalRecorder =
            MatchGoalRecorder(
                registerGoal = get(),
                getMatchById = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            ),
        reportExporter =
            MatchReportExporter(
                getMatchReportData = get(),
                exportMatchReportToPdf = get(),
                analyticsTracker = get(),
                crashReporter = get(),
            ),
    )
