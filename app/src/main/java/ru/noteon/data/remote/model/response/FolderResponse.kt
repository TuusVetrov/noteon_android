package ru.noteon.data.remote.model.response

data class FolderResponse(
    override val status: State,
    override val message: String,
    val folderId: String?
): BaseResponse