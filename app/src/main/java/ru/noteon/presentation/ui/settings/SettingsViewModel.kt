package ru.noteon.presentation.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.noteon.core.preference_manager.PreferenceManager
import ru.noteon.core.token.TokenManager
import ru.noteon.data.repository.RemoteUserRepository
import ru.noteon.domain.task.FolderTaskManager
import ru.noteon.domain.task.TaskManager
import ru.noteon.presentation.ui.sign_up.SignUpState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel@Inject constructor(
    private val repository: RemoteUserRepository,
    private val preferenceManager: PreferenceManager,
    private val folderTaskManager: FolderTaskManager,
    private val notesTaskManager: TaskManager,
    private val tokenManager: TokenManager,
): ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState.init)
    val uiState: StateFlow<SettingsState> = _uiState

    init {
        observeUser()
    }

    fun observeUser() {
        repository.getUser()
            .distinctUntilChanged()
            .onEach { response ->
                response.onSuccess { user ->
                    Log.d("UserDataLog", "${user.username} ${user.email}")
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            id = user.id,
                            username = user.username,
                            email = user.email,
                        )
                    }
                }.onFailure { message ->
                    Log.d("UserDataLog", message)
                    _uiState.update { currentState ->
                        currentState.copy(
                            errorMessage = message
                        )
                    }
                }
            }.onStart {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = true,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.saveTokens(null, null)
            notesTaskManager.abortAllTasks()
            folderTaskManager.abortAllTasks()
        }
    }

    suspend fun isDarkModeEnabled() = preferenceManager.uiModeFlow.first()

    fun setDarkMode(enable: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(enable)
        }
    }
}