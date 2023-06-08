package ru.noteon.data.remote.model.request

data class RegistrationRequest (
    val username: String,
    val email: String,
    val password: String
)