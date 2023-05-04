package ru.noteon.presentation.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.noteon.core.token.TokenManager
import ru.noteon.data.repository.UserRepository
import ru.noteon.utils.validators.AuthValidator
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
    private val tokenManager: TokenManager,
): ViewModel() {
    private val _uiState = MutableStateFlow(LoginState.init)
    val uiState: StateFlow<LoginState> = _uiState

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

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null
            )
        }
    }

    fun loginUser() {
        if(!validateCredentials())
            return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }

            val email = uiState.value.email
            val password = uiState.value.password

            val response = repository.getUserByEmailAndPassword(email, password)

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
        val isValidEmail = AuthValidator.isValidEmail(uiState.value.email)
        val isValidPassword = AuthValidator.isValidPassword(uiState.value.password)

        _uiState.update { currentState ->
            currentState.copy(
                isValidEmail = isValidEmail,
                isValidPassword = isValidPassword,
            )
        }

        return isValidEmail && isValidPassword
    }
}