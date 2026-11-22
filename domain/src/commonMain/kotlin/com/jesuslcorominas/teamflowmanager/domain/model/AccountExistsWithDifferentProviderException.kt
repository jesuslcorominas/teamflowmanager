package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * The email of the identity the user signed in with already belongs to an
 * account created with another provider (for instance Google). The accounts are
 * not linked automatically: the user is asked to sign in with the original one.
 */
class AccountExistsWithDifferentProviderException(
    cause: Throwable? = null,
) : Exception("An account already exists with the same email and a different sign-in provider", cause)
