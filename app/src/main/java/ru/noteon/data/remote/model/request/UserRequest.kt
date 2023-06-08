package ru.noteon.data.remote.model.request

data class ChangeUserDataRequest(
    val username: String,
    val email: String
)