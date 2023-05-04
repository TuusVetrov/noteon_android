package ru.noteon.domain.model

data class NoteModel(
    val id: String,
    val folder: String,
    val title: String,
    val body: String,
    val created: Long,
    val isPinned: Boolean = false
)
