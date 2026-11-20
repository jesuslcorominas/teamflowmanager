package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import platform.Foundation.NSLog

/**
 * Leaves a trace in the unified log when a Firestore read degrades to an empty/null result,
 * so a failing query or an unparseable document is never swallowed silently.
 */
internal fun logFirestoreRead(
    source: String,
    message: String,
    error: Throwable? = null,
) {
    val detail = error?.let { " -> ${it::class.simpleName}: ${it.message}" } ?: ""
    // No varargs: passing a Kotlin String through NSLog's C varargs crashes (SIGSEGV).
    NSLog("TFMFirestore [$source] $message$detail".replace("%", "%%"))
}
