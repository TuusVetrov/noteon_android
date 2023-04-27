package ru.noteon.data.remote.model.response

import ru.noteon.domain.model.NoteModel

data class GetAllNotesResponse(
    override val status: State,
    override val message: String,
    val notes: List<NoteModel> = emptyList()
): BaseResponse