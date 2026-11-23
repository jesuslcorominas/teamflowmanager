package com.jesuslcorominas.teamflowmanager.ui.players.wizard

/**
 * Master switch for the player photo, on both platforms.
 *
 * Off while photo upload does not work on iOS (#360); the full removal is tracked in #544. When
 * off, the wizard neither offers the picker (whatever the remote flag says) nor shows a stored
 * photo: every player gets the default avatar. The stored image URI is kept untouched.
 */
internal const val PLAYER_PHOTO_ENABLED = false
