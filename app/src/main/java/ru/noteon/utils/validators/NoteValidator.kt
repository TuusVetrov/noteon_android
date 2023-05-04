package ru.noteon.utils.validators

object NoteValidator {
    fun isValidNote(title: String, note: String) = (title.trim().isNotBlank() && note.trim().isNotBlank())
}
