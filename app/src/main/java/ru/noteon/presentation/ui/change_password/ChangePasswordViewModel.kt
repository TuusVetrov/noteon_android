package ru.noteon.presentation.ui.change_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.noteon.data.repository.RemoteUserRepository
import ru.noteon.utils.validators.AuthValidator
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val repository: RemoteUserRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordState.init)
    val uiState: StateFlow<ChangePasswordState> = _uiState

    fun setPassword(password: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    password = password
                )
            }
        }
    }

    fun setConfirmPassword(confirmPassword: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    confirmPassword = confirmPassword
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

    fun updatePassword() {
        if(!validateCredentials())
            return

        viewModelScope.launch {
            val password = uiState.value.password

            val response = repository.changePassword(password)

            response.onSuccess { response ->
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = null,
                        isSuccessful = true
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
        val password = uiState.value.password
        val confirmPassword = uiState.value.confirmPassword

        val isValidPassword = AuthValidator.isValidPassword(password)
        val arePasswordAndConfirmPasswordSame = AuthValidator.isPasswordAndConfirmPasswordSame(
            password,
            confirmPassword
        )

        _uiState.update { currentState ->
            currentState.copy(
                isValidPassword = isValidPassword,
                isPasswordsAreSame = arePasswordAndConfirmPasswordSame
            )
        }

        return isValidPassword && arePasswordAndConfirmPasswordSame
    }
}