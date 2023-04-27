package ru.noteon.data.remote.model.response

data class NoteResponse(
    override val status: State,
    override val message: String,
    val noteId: String?
): BaseResponse