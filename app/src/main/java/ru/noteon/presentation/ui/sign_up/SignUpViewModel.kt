package ru.noteon.presentation.ui.sign_up

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.noteon.core.token.TokenManager
import ru.noteon.data.repository.RemoteUserRepository
import ru.noteon.utils.validators.AuthValidator
import ru.noteon.utils.validators.UserDataValidator
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: RemoteUserRepository,
    private val tokenManager: TokenManager,
): ViewModel() {
    private val _uiState = MutableStateFlow(SignUpState.init)
    val uiState: StateFlow<SignUpState> = _uiState

    fun setEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email
            )
        }
    }

    fun setPassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password
            )
        }
    }

    fun setUsername(username: String) {
        _uiState.update { currentState ->
            currentState.copy(
                username = username
            )
        }
    }

    fun setConfirmPassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmPassword = password
            )
        }
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null
            )
        }
    }

    fun signUp() {
        if(!validateCredentials())
            return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }

            val username = uiState.value.username
            val email = uiState.value.email
            val password = uiState.value.password

            val response = repository.registration(username, email, password)

            response.onSuccess { authCredential ->
                tokenManager.saveTokens(authCredential.accessToken, authCredential.refreshToken)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null,
                    )
                }
            }.onFailure { message ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = message,
                    )
                }
            }
        }
    }

    private fun validateCredentials(): Boolean {
        val username = uiState.value.username
        val email = uiState.value.email
        val password = uiState.value.password
        val confirmPassword = uiState.value.confirmPassword

        val isValidUsername = UserDataValidator.isValidUsername(username)

        val isValidEmail = AuthValidator.isValidEmail(email)
        val isValidPassword = AuthValidator.isValidPassword(password)
        val arePasswordAndConfirmPasswordSame = AuthValidator.isPasswordAndConfirmPasswordSame(
            password,
            confirmPassword
        )

        _uiState.update { currentState ->
            currentState.copy(
                isValidUsername = isValidUsername,
                isValidEmail = isValidEmail,
                isValidPassword = isValidPassword,
                isValidConfirmPassword = arePasswordAndConfirmPasswordSame
            )
        }

        return isValidEmail && isValidPassword && arePasswordAndConfirmPasswordSame && isValidUsername
    }
}