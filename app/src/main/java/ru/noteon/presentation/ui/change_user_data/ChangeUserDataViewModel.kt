package ru.noteon.presentation.ui.change_user_data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.noteon.data.repository.RemoteUserRepository
import ru.noteon.presentation.ui.change_password.ChangePasswordState
import ru.noteon.utils.validators.AuthValidator
import ru.noteon.utils.validators.UserDataValidator
import javax.inject.Inject

@HiltViewModel
class ChangeUserDataViewModel  @Inject constructor(
    private val repository: RemoteUserRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ChangeDataState.init)
    val uiState: StateFlow<ChangeDataState> = _uiState

    fun setUsername(username: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    username = username
                )
            }
        }
    }

    fun setEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    email = email
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(
                isSuccessful = false,
                errorMessage = null
            )
        }
    }

    fun updateUserData() {
        if(!validateCredentials())
            return

        viewModelScope.launch {
            val username = uiState.value.username
            val email = uiState.value.email

            val response = repository.updateUserData(username, email)

            response.onSuccess { _ ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isSuccessful = true,
                        errorMessage = null,
                    )
                }
            }.onFailure { message ->
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = message,
                    )
                }
            }
        }
    }

    private fun validateCredentials(): Boolean {
        val username = uiState.value.username
        val email = uiState.value.email

        val isValidUsername = UserDataValidator.isValidUsername(username)
        val isValidEmail = UserDataValidator.isValidEmail(email)

        _uiState.update { currentState ->
            currentState.copy(
                isValidEmail = isValidEmail,
                isValidUsername = isValidUsername
            )
        }

        return isValidUsername && isValidEmail
    }
}
