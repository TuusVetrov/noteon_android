package ru.noteon.utils.validators

object AuthValidator {
    fun isValidEmail(email: String): Boolean = email.trim().length in (4..30)
    fun isValidPassword(password: String): Boolean = password.trim().length in (8..50)

    fun isPasswordAndConfirmPasswordSame(
        password: String,
        confirmedPassword: String
    ): Boolean = password.trim() == confirmedPassword.trim()
}