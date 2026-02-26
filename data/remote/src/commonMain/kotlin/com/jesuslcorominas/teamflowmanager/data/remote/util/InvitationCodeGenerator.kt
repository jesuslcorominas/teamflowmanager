package com.jesuslcorominas.teamflowmanager.data.remote.util

import kotlin.random.Random

/**
 * Generates readable alphanumeric invitation codes.
 * Excludes ambiguous characters (0, O, 1, I, l) for better readability.
 */
object InvitationCodeGenerator {
    // Readable characters excluding ambiguous ones: 0, O, 1, I, l
    private const val READABLE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val CODE_LENGTH = 8

    /**
     * Generates a unique, readable invitation code.
     * @param length The length of the code (default 8 characters)
     * @return A readable alphanumeric code
     */
    fun generate(length: Int = CODE_LENGTH): String {
        require(length in 6..10) { "Code length must be between 6 and 10 characters" }
        
        return buildString {
            repeat(length) {
                val randomIndex = Random.nextInt(READABLE_CHARS.length)
                append(READABLE_CHARS[randomIndex])
            }
        }
    }
}
