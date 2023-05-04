package ru.noteon.presentation.ui.edit_note

data class EditNoteState(
    val title: String?,
    val body: String?,
    val folder: String?,
    val isPinned: Boolean,
    val isSaving: Boolean,
    val finished: Boolean,
    val error: String?,
) {
    companion object {
        val init = EditNoteState(
            title = null,
            body = null,
            folder = null,
            isPinned = false,
            isSaving = false,
            finished = true,
            error = null
        )
    }
}