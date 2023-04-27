package ru.noteon.data.remote.model.response

data class AuthResponse(
    override val status: State,
    override val message: String,
    val accessToken: String?,
    val refreshToken: String?,
): BaseResponse