package ru.noteon.presentation.ui.sign_up

data class SignUpState(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val isLoading: Boolean,
    val isLoggedIn: Boolean,
    val isValidEmail: Boolean?,
    val isValidPassword: Boolean?,
    val isValidConfirmPassword: Boolean?,
    val errorMessage: String?,
) {
    companion object {
        val init = SignUpState(
            email = "",
            password = "",
            confirmPassword = "",
            isLoading = false,
            isLoggedIn = false,
            isValidEmail = null,
            isValidPassword = null,
            isValidConfirmPassword = null,
            errorMessage = null,
        )
    }
}