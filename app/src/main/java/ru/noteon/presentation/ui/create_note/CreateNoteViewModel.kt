package ru.noteon.presentation.ui.create_note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.noteon.core.task.NoteTask
import ru.noteon.core.task.TaskManager
import ru.noteon.data.repository.LocalNoteRepository
import ru.noteon.utils.validators.NoteValidator
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val repository: LocalNoteRepository,
    private val taskManager: TaskManager,
): ViewModel() {
    private val _uiState = MutableStateFlow(CreateNoteState.init)
    val uiState: StateFlow<CreateNoteState> = _uiState

    private var job: Job? = null

    fun setTitle(title: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = title
            )
        }
    }

     fun setFolder(folderId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                folder = folderId
            )
        }
    }

    fun setBody(body: String) {
        _uiState.update { currentState ->
            currentState.copy(
                body = body
            )
        }
    }

    fun add() {
        job?.cancel()

        job = viewModelScope.launch {
            val title = uiState.value.title.trim()
            val body = uiState.value.body.trim()
            val folder = uiState.value.folder.trim()

            if (!isNoteValid()) {
                return@launch
            }

            _uiState.update { currentState ->
                currentState.copy(
                    isAdding = true
                )
            }

            val result = repository.addNote(title, body, folder)

            result.onSuccess { noteId ->
                scheduleNoteCreate(noteId)
                _uiState.update { currentState ->
                    currentState.copy(
                        isAdding = false,
                        isAdded = true,
                    )
                }
            }.onFailure { message ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isAdding = false,
                        isAdded = false,
                        error = message
                    )
                }
            }
        }
    }

    private fun isNoteValid(): Boolean {
        val currentTitle = uiState.value.title
        val currentBody = uiState.value.body
        return NoteValidator.isValidNote(currentTitle, currentBody)
    }

    private fun scheduleNoteCreate(noteId: String) =
        taskManager.scheduleTask(NoteTask.create(noteId))

    fun resetState() {
        _uiState.update { currentState ->
            currentState.copy(
                title = "",
                body = "",
                showSave = false,
                isAdding = false,
                isAdded = false,
                error = null
            )
        }
    }
}