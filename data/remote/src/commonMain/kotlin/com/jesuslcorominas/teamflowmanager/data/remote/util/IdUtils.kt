package com.jesuslcorominas.teamflowmanager.data.remote.util

import kotlin.math.abs

/**
 * Generates a deterministic Long ID from a String document ID.
 * Uses a simplified hash function that is more predictable than hashCode().
 * The ID is based on the ASCII values of the characters to ensure consistency.
 */
fun String.toStableId(): Long {
    if (isEmpty()) return 0L
    var result = 0L
    var multiplier = 1L
    for (char in this) {
        result += char.code * multiplier
        multiplier *= 31
    }
    return abs(result)
}
