package ru.noteon.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import ru.noteon.core.utils.Either
import ru.noteon.data.remote.api.NoteService
import ru.noteon.data.remote.model.request.NoteRequest
import ru.noteon.data.remote.model.request.UpdatePinRequest
import ru.noteon.data.remote.model.response.State
import ru.noteon.data.remote.util.getResponse
import ru.noteon.domain.model.NoteModel
import javax.inject.Singleton

@Singleton
class RemoteNoteRepository(
   private val noteService: NoteService
) {
    fun getAllNotes(): Flow<Either<List<NoteModel>>> = flow {
        val notesResponse = noteService.getAllNotes().getResponse()

        val state = when (notesResponse.status) {
            State.SUCCESS -> Either.success(notesResponse.notes)
            else -> Either.error(notesResponse.message)
        }

        emit(state)
    }.catch { emit(Either.error("Can't sync latest notes")) }

    suspend fun addNote(title: String, body: String): Either<String> {
        return runCatching {
            val notesResponse = noteService.addNote(NoteRequest(title, body)).getResponse()

            when (notesResponse.status) {
                State.SUCCESS -> Either.success(notesResponse.noteId!!)
                else -> Either.error(notesResponse.message)
            }
        }.getOrElse {
            it.printStackTrace()
            (Either.error("Something went wrong!"))
        }
    }

    suspend fun updateNote(
        noteId: String,
        title: String,
        body: String
    ): Either<String> {
        return runCatching {
            val notesResponse = noteService.updateNote(
                noteId,
                NoteRequest(title, body)
            ).getResponse()

            when (notesResponse.status) {
                State.SUCCESS -> Either.success(notesResponse.noteId!!)
                else -> Either.error(notesResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }

    suspend fun deleteNote(noteId: String): Either<String> {
        return runCatching {
            val notesResponse = noteService.deleteNote(noteId).getResponse()

            when (notesResponse.status) {
                State.SUCCESS -> Either.success(notesResponse.noteId!!)
                else -> Either.error(notesResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }

    suspend fun pinNote(noteId: String, isPinned: Boolean): Either<String> {
        return runCatching {
            val notesResponse =
                noteService.updateNotePin(noteId, UpdatePinRequest(isPinned)).getResponse()

            when (notesResponse.status) {
                State.SUCCESS -> Either.success(notesResponse.noteId!!)
                else -> Either.error(notesResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }
}