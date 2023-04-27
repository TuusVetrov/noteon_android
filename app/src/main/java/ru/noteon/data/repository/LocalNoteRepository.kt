package ru.noteon.data.repository

import kotlinx.coroutines.flow.*
import ru.noteon.core.utils.Either
import ru.noteon.data.local.dao.NotesDao
import ru.noteon.data.local.entity.NoteEntity
import ru.noteon.domain.model.NoteModel
import java.util.*
import javax.inject.Inject

class LocalNoteRepository @Inject constructor(
    private val notesDao: NotesDao
) {
    companion object {
        private const val PREFIX_TEMP_NOTE_ID = "TMP"
        fun generateTemporaryId() = "$PREFIX_TEMP_NOTE_ID-${UUID.randomUUID()}"
    }

    fun isTemporaryNote(noteId: String) = noteId.startsWith(PREFIX_TEMP_NOTE_ID)

    fun getNoteById(noteId: String): Flow<NoteModel> = notesDao.getNoteById(noteId)
        .filterNotNull()
        .map { NoteModel(it.id, it.title, it.body, it.created, it.isPinned) }

    fun getAllNotes(): Flow<Either<List<NoteModel>>> = notesDao.getAllNotes()
        .map { notes -> notes.map { NoteModel(it.id, it.title, it.body, it.created, it.isPinned) } }
        .transform { notes -> emit(Either.success(notes)) }
        .catch { emit(Either.success(emptyList())) }

    suspend fun addNote(
        title: String,
        body: String
    ): Either<String> = runCatching {
        val tmpNoteId = generateTemporaryId()
        notesDao.addNote(
            NoteEntity(
                id = tmpNoteId,
                title = title,
                body = body,
                created = System.currentTimeMillis(),
                isPinned = false
            )
        )
        Either.success(tmpNoteId)
    }.getOrDefault(Either.error("Unable to create a new note"))

    suspend fun addNotes(notes: List<NoteModel>) = notes.map {
        NoteEntity(it.id, it.title, it.body, it.created, it.isPinned)
    }.let {
        notesDao.addNotes(it)
    }

    suspend fun updateNote(
        noteId: String,
        title: String,
        body: String
    ): Either<String> = runCatching {
        notesDao.updateNoteById(noteId, title, body)
        Either.success(noteId)
    }.getOrDefault(Either.error("Unable to update a note"))

    suspend fun deleteNote(noteId: String): Either<String> = runCatching {
        notesDao.deleteNoteById(noteId)
        Either.success(noteId)
    }.getOrDefault(Either.error("Unable to delete a note"))

    suspend fun pinNote(noteId: String, isPinned: Boolean): Either<String> = runCatching {
        notesDao.updateNotePin(noteId, isPinned)
        Either.success(noteId)
    }.getOrDefault(Either.error("Unable to pin the note"))

    suspend fun deleteAllNotes() = notesDao.deleteAllNotes()

    suspend fun updateNoteId(oldNoteId: String, newNoteId: String) =
        notesDao.updateNoteId(oldNoteId, newNoteId)
}