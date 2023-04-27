package ru.noteon.domain.model

data class AuthCredential(
    val accessToken: String,
    val refreshToken: String
)