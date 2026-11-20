package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.ios
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.Foundation.NSNumber

/**
 * Raw field map of a Firestore document, read from the native iOS snapshot.
 *
 * `doc.data<Map<String, Any?>>()` cannot work on iOS: GitLive decodes through
 * kotlinx.serialization and there is no serializer for `Any`, so every call throws and the
 * datasources used to drop every document silently (#503). Android reads the native map
 * (`DocumentSnapshot.getData()`); this is the iOS equivalent, feeding the same shared parsers.
 *
 * Native `NSNumber` values are unboxed to Kotlin [Boolean], [Double] or [Long] so the parsers'
 * `as? Number` / `as? Boolean` checks behave like on Android.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun DocumentSnapshot.rawData(): Map<String, Any?>? = ios.data()?.entries?.associate { (key, value) -> key.toString() to value.toKotlinValue() }

@OptIn(ExperimentalForeignApi::class)
private fun Any?.toKotlinValue(): Any? =
    when (this) {
        is NSNumber ->
            when (objCType?.toKString()) {
                // Firestore booleans are CFBoolean, whose objCType is "c".
                "c", "B" -> boolValue
                "f", "d" -> doubleValue
                else -> longLongValue
            }
        else -> this
    }
