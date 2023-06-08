package ru.noteon.presentation.ui.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import ru.noteon.data.repository.LocalFolderRepository
import ru.noteon.presentation.ui.list_folders.FoldersState
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val foldersRepository: LocalFolderRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(FoldersState.init)
    val uiState: StateFlow<FoldersState> = _uiState

    init {
        observeFolders()
    }

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
}