package ru.noteon.data.repository

import android.util.Log
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
        .map { NoteModel(it.noteId, it.folder, it.title, it.body, it.created, it.isPinned) }

    fun getAllNotes(): Flow<Either<List<NoteModel>>> = notesDao.getAllNotes()
        .map { notes -> notes.map { NoteModel(it.noteId, it.folder, it.title, it.body, it.created, it.isPinned) } }
        .transform { notes -> emit(Either.success(notes)) }
        .catch { emit(Either.success(emptyList())) }


    fun getAllNotesFromFolderId(folderId: String): Flow<Either<List<NoteModel>>> = notesDao.getAllNotesFromFolderId(folderId)
        .map { notes -> notes.map { NoteModel(it.noteId, it.folder, it.title, it.body, it.created, it.isPinned) } }
        .transform { notes -> emit(Either.success(notes)) }
        .catch { emit(Either.success(emptyList())) }

    fun searchNote(searchQuery: String, folderId: String): Flow<List<NoteModel>> = notesDao.searchNote(searchQuery, folderId)
        .map { notes -> notes.map { NoteModel(it.noteId, it.folder, it.title, it.body, it.created, it.isPinned) } }

    suspend fun addNote(
        title: String,
        body: String,
        folderId: String,
    ): Either<String> = runCatching {
        val tmpNoteId = generateTemporaryId()
        notesDao.addNote(
            NoteEntity(
                noteId = tmpNoteId,
                folder = folderId,
                title = title,
                body = body,
                created = System.currentTimeMillis(),
                isPinned = false
            )
        )
        Either.success(tmpNoteId)
    }.getOrDefault(Either.error("Невозможно создать новую заметку"))

    suspend fun addNotes(notes: List<NoteModel>) = notes.map {
        NoteEntity(it.id, it.title, it.body, it.created, it.isPinned, it.folder)
    }.let {
        notesDao.addNotes(it)
    }

  /*  suspend fun updateNote(
        noteId: String,
        title: String,
        body: String,
        folderId: String,
    ): Either<String> = runCatching {
        Log.d("meme", "asdas")
        notesDao.testUpdateNoteById(noteId)
        Log.d("meme", "sad")
        Either.success(noteId)
    }.getOrDefault(Either.error("Unable to update a note"))*/

    suspend fun updateNote(
        noteId: String,
        title: String,
        note: String,
        folderId: String
    ): Either<String> = runCatching {
        notesDao.updateNoteById(noteId, folderId, title, note)
        Either.success(noteId)
    }.getOrDefault(Either.error("Не удалось обновить заметку"))


    suspend fun deleteNote(noteId: String): Either<String> = runCatching {
        notesDao.deleteNoteById(noteId)
        Either.success(noteId)
    }.getOrDefault(Either.error("Не удалось удалить заметку"))

    suspend fun pinNote(noteId: String, isPinned: Boolean): Either<String> = runCatching {
        notesDao.updateNotePin(noteId, isPinned)
        Either.success(noteId)
    }.getOrDefault(Either.error("Не удалось закрепить заметку"))

    suspend fun deleteAllNotes() = notesDao.deleteAllNotes()

    suspend fun updateNoteId(oldNoteId: String, newNoteId: String) =
        notesDao.updateNoteId(oldNoteId, newNoteId)
}