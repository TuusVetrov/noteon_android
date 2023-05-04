package ru.noteon.presentation.ui.create_note

data class CreateNoteState(
    val title: String,
    val body: String,
    val folder: String,
    val showSave: Boolean,
    val isAdding: Boolean,
    val isAdded: Boolean,
    val error: String?,
) {
    companion object {
        val init = CreateNoteState(
            title = "",
            body = "",
            folder = "",
            showSave = false,
            isAdding = false,
            isAdded = false,
            error = null
        )
    }
}