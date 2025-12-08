package com.jesuslcorominas.teamflowmanager.domain.analytics

/**
 * Analytics event names used throughout the application.
 * Centralized constant definitions to avoid typos and ensure consistency.
 */
object AnalyticsEvent {
    // Team Management Events
    const val TEAM_CREATED = "team_created"
    const val TEAM_UPDATED = "team_updated"
    const val TEAM_DELETED = "team_deleted"
    const val TEAM_VIEWED = "team_viewed"

    // Player Management Events
    const val PLAYER_CREATED = "player_created"
    const val PLAYER_UPDATED = "player_updated"
    const val PLAYER_DELETED = "player_deleted"
    const val PLAYER_PHOTO_ADDED = "player_photo_added"

    // Match Management Events
    const val MATCH_CREATED = "match_created"
    const val MATCH_UPDATED = "match_updated"
    const val MATCH_STARTED = "match_started"
    const val MATCH_PAUSED = "match_paused"
    const val MATCH_RESUMED = "match_resumed"
    const val MATCH_FINISHED = "match_finished"
    const val MATCH_ARCHIVED = "match_archived"
    const val MATCH_UNARCHIVED = "match_unarchived"
    const val MATCH_DELETED = "match_deleted"
    const val MATCH_VIEWED = "match_viewed"

    // Match Actions Events
    const val SUBSTITUTION_MADE = "substitution_made"
    const val GOAL_SCORED = "goal_scored"
    const val OPPONENT_GOAL_SCORED = "opponent_goal_scored"
    const val CARD_ISSUED = "card_issued"
    const val STARTING_LINEUP_SET = "starting_lineup_set"
    const val CAPTAIN_SELECTED = "captain_selected"
    const val CAPTAIN_REMOVED = "captain_removed"

    // Export Events
    const val PDF_EXPORTED = "pdf_exported"
    const val MATCH_REPORT_EXPORTED = "match_report_exported"
    const val PLAYER_STATS_EXPORTED = "player_stats_exported"
    const val DATABASE_EXPORTED = "database_exported"
    const val DATABASE_IMPORTED = "database_imported"
    const val DATABASE_IMPORT_CANCELLED = "database_import_cancelled"

    // Navigation Events
    const val SCREEN_VIEW = "screen_view"
    const val WIZARD_STEP_COMPLETED = "wizard_step_completed"
    const val WIZARD_CANCELLED = "wizard_cancelled"
    const val BUTTON_CLICKED = "button_clicked"
    const val FAB_CLICKED = "fab_clicked"

    // Dialog Events
    const val DIALOG_SHOWN = "dialog_shown"
    const val DIALOG_CONFIRMED = "dialog_confirmed"
    const val DIALOG_CANCELLED = "dialog_cancelled"

    // Statistics Events
    const val STATS_VIEWED = "stats_viewed"
    const val CHART_VIEWED = "chart_viewed"
    const val TAB_CHANGED = "tab_changed"
}

/**
 * Analytics parameter names used throughout the application.
 * Centralized constant definitions to avoid typos and ensure consistency.
 */
object AnalyticsParam {
    // Common Parameters
    const val TEAM_ID = "team_id"
    const val TEAM_NAME = "team_name"
    const val TEAM_CATEGORY = "team_category"
    const val PLAYER_ID = "player_id"
    const val PLAYER_NAME = "player_name"
    const val PLAYER_NUMBER = "player_number"
    const val PLAYER_POSITION = "player_position"
    const val MATCH_ID = "match_id"
    const val MATCH_TYPE = "match_type"

    // Match Action Parameters
    const val PLAYER_IN = "player_in"
    const val PLAYER_OUT = "player_out"
    const val TEAM_TYPE = "team_type"
    const val CARD_TYPE = "card_type"
    const val FORMATION = "formation"
    const val DURATION_MINUTES = "duration_minutes"
    const val GOAL_MINUTE = "goal_minute"
    const val SUBSTITUTION_MINUTE = "substitution_minute"
    const val SUBSTITUTION_METHOD = "substitution_method"

    // Navigation Parameters
    const val SCREEN_NAME = "screen_name"
    const val SCREEN_CLASS = "screen_class"
    const val WIZARD_TYPE = "wizard_type"
    const val STEP_NUMBER = "step_number"
    const val BUTTON_NAME = "button_name"
    const val DIALOG_TYPE = "dialog_type"

    // Statistics Parameters
    const val STATS_TYPE = "stats_type"
    const val CHART_TYPE = "chart_type"
    const val TIME_RANGE = "time_range"
    const val TAB_NAME = "tab_name"
    const val EXPORT_TYPE = "export_type"

    // Error Parameters
    const val ERROR_MESSAGE = "error_message"
    const val ERROR_TYPE = "error_type"
    const val ERROR_LOCATION = "error_location"

    // Import/Export Parameters
    const val IMPORT_SOURCE = "import_source"
    const val IMPORT_SUCCESS = "import_success"
    const val EXPORT_SUCCESS = "export_success"
}

/**
 * Screen names used for analytics tracking.
 * Centralized constant definitions to avoid typos and ensure consistency.
 */
object ScreenName {
    const val TEAM = "Team"
    const val PLAYERS = "Players"
    const val MATCHES = "Matches"
    const val MATCH_DETAIL = "Match Detail"
    const val ARCHIVED_MATCHES = "Archived Matches"
    const val ANALYSIS = "Analysis"
    const val PLAYER_WIZARD = "Player Wizard"
    const val MATCH_WIZARD = "Match Wizard"
    const val SPLASH = "Splash"
    const val LOGIN = "Login"
    const val MIGRATION = "Migration"
    const val SETTINGS = "Settings"
}
