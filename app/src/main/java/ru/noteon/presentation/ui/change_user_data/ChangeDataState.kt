package ru.noteon.presentation.ui.change_user_data

data class ChangeDataState(
    val username: String,
    val email: String,
    val isValidUsername: Boolean?,
    val isValidEmail: Boolean?,
    val isSuccessful: Boolean?,
    val errorMessage: String?,
) {
    companion object {
        val init = ChangeDataState(
            username = "",
            email = "",
            isValidUsername = null,
            isValidEmail = null,
            isSuccessful = null,
            errorMessage = null,
        )
    }
}