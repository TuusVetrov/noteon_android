package ru.noteon.presentation.ui.list_notes

import ru.noteon.domain.model.NoteModel

data class NotesState(
    val isLoading: Boolean,
    val notes: List<NoteModel>,
    val searchNotes: List<NoteModel>,
    val error: String?,
    val isUserLoggedIn: Boolean?,
    val isConnectivityAvailable: Boolean?
) {
    companion object {
        val init = NotesState(
            isLoading = false,
            notes = emptyList(),
            searchNotes = emptyList(),
            error = null,
            isUserLoggedIn = null,
            isConnectivityAvailable = null
        )
    }
}