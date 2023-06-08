package ru.noteon.data.remote.model.response

data class UserDataChangeResponse(
    override val status: State,
    override val message: String,
    val id: String?,
): BaseResponse