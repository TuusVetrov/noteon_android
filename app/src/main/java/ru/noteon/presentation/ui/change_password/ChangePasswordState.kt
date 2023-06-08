package ru.noteon.presentation.ui.change_password

data class ChangePasswordState(
    val password: String,
    val confirmPassword: String,
    val isValidPassword: Boolean?,
    val isPasswordsAreSame: Boolean?,
    val isSuccessful: Boolean?,
    val errorMessage: String?,
) {
    companion object {
        val init = ChangePasswordState(
            password = "",
            confirmPassword = "",
            isValidPassword = null,
            isPasswordsAreSame = null,
            isSuccessful = null,
            errorMessage = null,
        )
    }
}