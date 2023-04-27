package ru.noteon.core.task

class NoteTask private constructor(val noteId: String, val action: TaskAction) {
    companion object {
        fun create(noteId: String) = NoteTask(noteId, TaskAction.CREATE)
        fun update(noteId: String) = NoteTask(noteId, TaskAction.UPDATE)
        fun delete(noteId: String) = NoteTask(noteId, TaskAction.DELETE)
        fun pin(noteId: String) = NoteTask(noteId, TaskAction.PIN)
    }
}

enum class TaskAction {
    CREATE, UPDATE, DELETE, PIN
}
