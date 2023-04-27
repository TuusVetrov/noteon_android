package ru.noteon.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.noteon.data.remote.model.request.NoteRequest
import ru.noteon.data.remote.model.request.UpdatePinRequest
import ru.noteon.data.remote.model.response.GetAllNotesResponse
import ru.noteon.data.remote.model.response.NoteResponse

interface NoteService {
    @GET("notes")
    suspend fun getAllNotes(): Response<GetAllNotesResponse>

    @POST("note/new")
    suspend fun addNote(@Body noteRequest: NoteRequest): Response<NoteResponse>

    @PUT("note/{noteId}")
    suspend fun updateNote(
        @Path("noteId") noteId: String,
        @Body noteRequest: NoteRequest
    ): Response<NoteResponse>

    @DELETE("note/{noteId}")
    suspend fun deleteNote(@Path("noteId") noteId: String): Response<NoteResponse>

    @PATCH("note/{noteId}/pin")
    suspend fun updateNotePin(
        @Path("noteId") noteId: String,
        @Body noteRequest: UpdatePinRequest
    ): Response<NoteResponse>
}