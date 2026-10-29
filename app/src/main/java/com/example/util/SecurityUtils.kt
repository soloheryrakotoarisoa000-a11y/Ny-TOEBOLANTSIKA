package com.example.util

import java.security.MessageDigest

object SecurityUtils {
    private const val SALT = "NyToebolantsikaSecured_Salt_2026"

    fun hashPin(pin: String): String {
        val input = "$pin:$SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return true
        return hashPin(pin) == storedHash
    }
}
