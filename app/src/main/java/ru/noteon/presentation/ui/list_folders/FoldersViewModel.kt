package ru.noteon.presentation.ui.list_folders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.noteon.core.connectivity.ConnectionState
import ru.noteon.core.connectivity.ConnectivityObserver
import ru.noteon.core.task.*
import ru.noteon.core.token.TokenManager
import ru.noteon.data.repository.LocalFolderRepository
import ru.noteon.domain.model.FolderModel
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val foldersRepository: LocalFolderRepository,
    private val tokenManager: TokenManager,
    private val taskManager: FolderTaskManager,
    private val connectivityObserver: ConnectivityObserver
): ViewModel() {
    private val _uiState = MutableStateFlow(FoldersState.init)
    val uiState: StateFlow<FoldersState> = _uiState

    private var syncJob: Job? = null
    private var job: Job? = null

    init {
        checkUserSession()
        observeFolders()
        syncFolders()
        observeConnectivity()
    }

    fun searchFolder(searchQuery: String): LiveData<List<FolderModel>> {
        return foldersRepository.searchFolder(searchQuery).asLiveData()
    }

    fun syncFolders() {
        if (tokenManager.getAccessToken() == null)
            return

        if(syncJob?.isActive == true)
            return

        syncJob = viewModelScope.launch {
            val taskId = taskManager.syncFolders()

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
                                    error = "Не удалось синхронизировать папки"
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

    fun delete(folderId: String) {
        job?.cancel()
        job = viewModelScope.launch {
            val response = foldersRepository.deleteFolder(folderId)

            response.onSuccess { noteId ->
                if (!foldersRepository.isTemporaryFolder(noteId)) {
                    scheduleFolderDelete(noteId)
                }
            }.onFailure { message ->
                _uiState.update { currentState ->
                    currentState.copy(
                        error = message,
                    )
                }
            }
        }
    }

    fun add(folderName: String) {
        job?.cancel()
        job = viewModelScope.launch {
            val response = foldersRepository.addFolder(folderName)

            response.onSuccess { folderId ->
                scheduleFolderCreate(folderId)
            }.onFailure { message ->
                _uiState.update { currentState ->
                    currentState.copy(
                        error = message,
                    )
                }
            }
        }
    }

    private fun scheduleFolderCreate(folderId: String) =
        taskManager.scheduleTask(FolderTask.create(folderId))

    private fun scheduleFolderDelete(folderId: String) =
        taskManager.scheduleTask(FolderTask.delete(folderId))

    private fun observeFolders() {
        foldersRepository.getAllFolders()
            .distinctUntilChanged()
            .onEach { response ->
                response.onSuccess { folders ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            folders = folders
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
        private const val TAG = "FoldersViewModel"
    }
}