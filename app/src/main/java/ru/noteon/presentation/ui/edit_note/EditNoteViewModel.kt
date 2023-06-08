package ru.noteon.presentation.ui.edit_note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.noteon.data.repository.LocalFolderRepository
import ru.noteon.domain.task.NoteTask
import ru.noteon.domain.task.TaskManager
import ru.noteon.data.repository.LocalNoteRepository
import ru.noteon.domain.model.NoteModel
import javax.inject.Inject

class EditNoteViewModel @AssistedInject constructor(
    private val repository: LocalNoteRepository,
    private val foldersRepository: LocalFolderRepository,
    private val taskManager: TaskManager,
    @Assisted private val noteId: String
): ViewModel() {
    private val _uiState = MutableStateFlow(EditNoteState.init)
    val uiState: StateFlow<EditNoteState> = _uiState

    private var job: Job? = null
    private lateinit var currentNote: NoteModel

    init {
        loadNote()
        observeFolders()
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

    fun setFolder(folder: String) {
        _uiState.update { currentState ->
            currentState.copy(
                folder = folder
            )
        }
    }


    private fun observeFolders() {
        foldersRepository.getAllFolders()
            .distinctUntilChanged()
            .onEach { response ->
                response.onSuccess { folders ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            folders = folders
                        )
                    }
                }.onFailure { message ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            error = message
                        )
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun loadNote() {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId).firstOrNull()
            if (note != null) {
                currentNote = note
                _uiState.update { currentState ->
                    currentState.copy(
                        title = note.title,
                        body = note.body,
                        folder = note.folder,
                        isPinned = note.isPinned
                    )
                }
            } else {
                _uiState.update { currentState ->
                    currentState.copy(

                    )
                }
            }
        }
    }

    fun save() {
        val title = uiState.value.title?.trim() ?: ""
        val body = uiState.value.body?.trim() ?: ""
        val folder = uiState.value.folder?.trim() ?: ""

      //  if (areOldAndUpdatedNoteSame()) {
      //      return
      //  }

        job?.cancel()
        job = viewModelScope.launch {
            val response = repository.updateNote(noteId, title, body, folder)
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
            val response = repository.deleteNote(noteId)

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
            val response = repository.pinNote(noteId, !uiState.value.isPinned)

            _uiState.update { currentState ->
                currentState.copy(
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

    private fun areOldAndUpdatedNoteSame(): Boolean {
        val oldTitle = currentNote.title
        val oldNote = currentNote.body
        val oldFolder = currentNote.folder

        val title = uiState.value.title
        val note = uiState.value.body
        val folder = uiState.value.folder

        return oldTitle == title?.trim() && oldNote == note?.trim() && oldFolder == folder?.trim()
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
        fun create(noteId: String): EditNoteViewModel
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