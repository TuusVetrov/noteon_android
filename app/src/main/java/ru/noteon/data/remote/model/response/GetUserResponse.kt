package ru.noteon.data.remote.model.response

data class GetUserResponse(
    override val status: State,
    override val message: String,
    val id: String,
    val username: String,
    val email: String
): BaseResponse