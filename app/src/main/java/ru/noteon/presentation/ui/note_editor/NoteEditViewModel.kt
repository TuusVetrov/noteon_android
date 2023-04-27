package ru.noteon.presentation.ui.note_editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.noteon.core.task.NoteTask
import ru.noteon.core.task.TaskManager
import ru.noteon.data.repository.LocalNoteRepository
import ru.noteon.domain.model.NoteModel
import ru.noteon.utils.validators.NoteValidator
import javax.inject.Inject

class NoteEditViewModel @AssistedInject constructor(
    private val repository: LocalNoteRepository,
    private val taskManager: TaskManager,
    @Assisted private val noteId: String
): ViewModel() {
    private val _uiState = MutableStateFlow(NoteEditState.init)
    val uiState: StateFlow<NoteEditState> = _uiState

    private var job: Job? = null
    private lateinit var currentNote: NoteModel

    init {
        loadNote()
    }

    fun setTitle(title: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = title
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

    private fun loadNote() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }
            val note = repository.getNoteById(noteId).firstOrNull()
            if (note != null) {
                currentNote = note
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        title = note.title,
                        body = note.body,
                        isPinned = note.isPinned
                    )
                }
            } else {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        finished = true,
                    )
                }
            }
        }
    }

    fun save() {
        val title = uiState.value.title?.trim() ?: return
        val body = uiState.value.body?.trim() ?: return

        job?.cancel()
        job = viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }

            val response = repository.updateNote(noteId, title, body)

            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false
                )
            }

            response.onSuccess { noteId ->
                if (repository.isTemporaryNote(noteId)) {
                    scheduleNoteCreate(noteId)
                } else {
                    scheduleNoteUpdate(noteId)
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        finished = true
                    )
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

    fun delete() {
        job?.cancel()
        job = viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }

            val response = repository.deleteNote(noteId)

            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false
                )
            }

            response.onSuccess { noteId ->
                if (!repository.isTemporaryNote(noteId)) {
                    scheduleNoteDelete(noteId)
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        finished = true
                    )
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

    fun togglePin() {
        job?.cancel()
        job = viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }

            val response = repository.pinNote(noteId, !uiState.value.isPinned)

            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    isPinned = !currentState.isPinned
                )
            }

            response.onSuccess { noteId ->
                if (!repository.isTemporaryNote(noteId)) {
                    scheduleNoteUpdatePin(noteId)
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

    private fun validateNote() {
        try {
            val oldTitle = currentNote.title
            val oldNote = currentNote.body

            val title = uiState.value.title
            val note = uiState.value.body

            val isValid = title != null && note != null && NoteValidator.isValidNote(title, note)
            val areOldAndUpdatedNoteSame = oldTitle == title?.trim() && oldNote == note?.trim()

            _uiState.update { currentState ->
                currentState.copy(
                    showSave = isValid && !areOldAndUpdatedNoteSame
                )
            }
        } catch (error: Throwable) {
        }
    }

    private fun scheduleNoteCreate(noteId: String) =
        taskManager.scheduleTask(NoteTask.create(noteId))

    private fun scheduleNoteUpdate(noteId: String) =
        taskManager.scheduleTask(NoteTask.update(noteId))

    private fun scheduleNoteDelete(noteId: String) =
        taskManager.scheduleTask(NoteTask.delete(noteId))

    private fun scheduleNoteUpdatePin(noteId: String) =
        taskManager.scheduleTask(NoteTask.pin(noteId))

    @AssistedFactory
    interface Factory {
        fun create(noteId: String): NoteEditViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            noteId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(noteId) as T
            }
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface AssistedInjectModule