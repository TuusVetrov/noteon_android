package ru.noteon.presentation.ui.note_editor

data class NoteEditState(
    val isLoading: Boolean,
    val title: String?,
    val body: String?,
    val isPinned: Boolean,
    val isSaving: Boolean,
    val finished: Boolean,
    val error: String?,
) {
    companion object {
        val init = NoteEditState(
            isLoading = false,
            title = null,
            body = null,
            isPinned = false,
            isSaving = false,
            finished = false,
            error = null
        )
    }
}
