package ru.noteon.presentation.ui.login

data class LoginState(
    val email: String,
    val password: String,
    val isLoading: Boolean,
    val isLoggedIn: Boolean,
    val isValidEmail: Boolean?,
    val isValidPassword: Boolean?,
    val errorMessage: String?,
) {
    companion object {
        val init = LoginState(
            email = "",
            password = "",
            isLoading = false,
            isLoggedIn = false,
            isValidEmail = null,
            isValidPassword = null,
            errorMessage = null,
        )
    }
}