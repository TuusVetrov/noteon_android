package ru.noteon.presentation.ui.list_notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.noteon.core.connectivity.ConnectionState
import ru.noteon.core.connectivity.ConnectivityObserver
import ru.noteon.core.task.TaskManager
import ru.noteon.core.task.TaskState
import ru.noteon.core.token.TokenManager
import ru.noteon.data.repository.LocalNoteRepository
import ru.noteon.presentation.ui.login.LoginState
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: LocalNoteRepository,
    private val tokenManager: TokenManager,
    private val taskManager: TaskManager,
    private val connectivityObserver: ConnectivityObserver
): ViewModel() {
    private val _uiState = MutableStateFlow(NotesState.init)
    val uiState: StateFlow<NotesState> = _uiState

    private var syncJob: Job? = null

    init {
        checkUserSession()
        observeNotes()
        syncNotes()
        observeConnectivity()
    }

    fun syncNotes() {
        if (tokenManager.getAccessToken() == null)
            return

        if(syncJob?.isActive == true)
            return

        syncJob = viewModelScope.launch {
            val taskId = taskManager.syncNotes()

            try {
                taskManager.observeTask(taskId).collect { taskState ->
                    when (taskState) {
                        TaskState.SCHEDULED -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = true
                                )
                            }
                        }
                        TaskState.COMPLETED, TaskState.CANCELLED -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false
                                )
                            }
                        }
                        TaskState.FAILED -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    error = "Не удалось синхронизировать заметки"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Can't find work by ID '$taskId'")
            }
        }
    }

    private fun observeNotes() {
        noteRepository.getAllNotes()
            .distinctUntilChanged()
            .onEach { response ->
                response.onSuccess { notes ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            notes = notes
                        )
                    }
                }.onFailure { message ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = message
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

    private fun observeConnectivity() {
        connectivityObserver.connectionState
            .distinctUntilChanged()
            .map { it === ConnectionState.Available }
            .onEach {
                _uiState.update { currentState ->
                    currentState.copy(
                        isConnectivityAvailable = it
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isUserLoggedIn = tokenManager.getAccessToken() != null
                )
            }
        }
    }

    companion object {
        private const val TAG = "NotesViewModel"
    }
}