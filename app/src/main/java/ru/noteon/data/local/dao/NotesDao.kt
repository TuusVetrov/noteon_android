package ru.noteon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.noteon.data.local.entity.NoteEntity

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes ORDER BY isPinned = 1 DESC, created DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert
    suspend fun addNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotes(notes: List<NoteEntity>)

    @Query("UPDATE notes SET title = :title, body = :body WHERE id = :noteId")
    suspend fun updateNoteById(noteId: String, title: String, body: String)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("UPDATE notes SET id = :newNoteId WHERE id = :oldNoteId")
    suspend fun updateNoteId(oldNoteId: String, newNoteId: String)

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :noteId")
    suspend fun updateNotePin(noteId: String, isPinned: Boolean)
}