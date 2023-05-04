package ru.noteon.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.noteon.data.local.entity.NoteEntity

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    fun getNoteById(noteId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes ORDER BY isPinned = 1 DESC, created DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folder = :folderId ORDER BY isPinned = 1 DESC, created DESC")
    fun getAllNotesFromFolderId(folderId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE (folder = :folderId) and (title LIKE :searchQuery OR body LIKE :searchQuery)")
    fun searchNote(searchQuery: String, folderId: String): Flow<List<NoteEntity>>

    @Insert
    suspend fun addNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotes(notes: List<NoteEntity>)

    @Query("UPDATE notes SET folder = :folderId, title = :title, body = :note WHERE noteId = :noteId")
    suspend fun updateNoteById(noteId: String, folderId: String, title: String, note: String)

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("UPDATE notes SET noteId = :newNoteId WHERE noteId = :oldNoteId")
    suspend fun updateNoteId(oldNoteId: String, newNoteId: String)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE noteId = :noteId")
    suspend fun updateNotePin(noteId: String, isPinned: Boolean)
}