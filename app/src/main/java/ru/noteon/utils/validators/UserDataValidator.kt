package ru.noteon.utils.validators

import android.util.Patterns

object UserDataValidator {
    fun isValidEmail(email: String): Boolean =
        email.trim().length in (4..128) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidUsername(username: String): Boolean = username.trim().length in (2 .. 128)
}