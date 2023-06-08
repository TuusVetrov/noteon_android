package ru.noteon.presentation.ui.sign_up

data class SignUpState(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val isLoading: Boolean,
    val isLoggedIn: Boolean,
    val isValidUsername: Boolean?,
    val isValidEmail: Boolean?,
    val isValidPassword: Boolean?,
    val isValidConfirmPassword: Boolean?,
    val errorMessage: String?,
) {
    companion object {
        val init = SignUpState(
            username = "",
            email = "",
            password = "",
            confirmPassword = "",
            isValidUsername = null,
            isLoading = false,
            isLoggedIn = false,
            isValidEmail = null,
            isValidPassword = null,
            isValidConfirmPassword = null,
            errorMessage = null,
        )
    }
}