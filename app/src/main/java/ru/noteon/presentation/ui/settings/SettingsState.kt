package ru.noteon.presentation.ui.settings

import ru.noteon.domain.model.UserModel
import ru.noteon.presentation.ui.sign_up.SignUpState

data class SettingsState(
    val id: String,
    val username: String,
    val email: String,
    val isLoading: Boolean,
    val errorMessage: String?,
) {
    companion object {
        val init = SettingsState(
            id = "",
            username = "",
            email = "",
            isLoading = false,
            errorMessage = null,
        )
    }
}
