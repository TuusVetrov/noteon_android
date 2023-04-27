package ru.noteon.data.remote.model.request

data class AuthRequest(
    val email: String,
    val password: String,
)