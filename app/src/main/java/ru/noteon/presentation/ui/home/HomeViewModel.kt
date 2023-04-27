package ru.noteon.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.noteon.core.token.TokenManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    tokenManager: TokenManager,
): ViewModel() {
    private val _uiState = MutableStateFlow(HomeState.init)
    val uiState: StateFlow<HomeState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoggedIn = tokenManager.getAccessToken() != null
                )
            }
        }
    }
}