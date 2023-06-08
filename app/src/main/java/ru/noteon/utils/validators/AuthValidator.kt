package ru.noteon.utils.validators

import android.util.Patterns

object AuthValidator {
    fun isValidEmail(email: String): Boolean =
        email.trim().length in (4..254) &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): Boolean = password.trim().length in (8..50)

    fun isPasswordAndConfirmPasswordSame(
        password: String,
        confirmedPassword: String
    ): Boolean = password.trim() == confirmedPassword.trim()
}