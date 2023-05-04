package ru.noteon.data.remote.model.response

import ru.noteon.domain.model.FolderModel

data class GetAllFoldersResponse(
    override val status: State,
    override val message: String,
    val folders: List<FolderModel> = emptyList(),
): BaseResponse