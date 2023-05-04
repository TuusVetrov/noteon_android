package ru.noteon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = false)
    val noteId: String,
    val title: String,
    val body: String,
    val created: Long,
    val isPinned: Boolean,
    val folder: String,
)