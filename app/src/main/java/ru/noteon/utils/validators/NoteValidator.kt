package ru.noteon.utils.validators

object NoteValidator {
    fun isValidNote(title: String, note: String) = (title.trim().length >= 4 && note.isNotBlank())
}
