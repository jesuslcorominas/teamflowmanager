package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * No club matches the invitation code the user typed. Typed so the UI can show a
 * translated message instead of the exception text.
 */
class ClubNotFoundException(
    invitationCode: String,
) : IllegalArgumentException("Club not found with invitation code: $invitationCode")
