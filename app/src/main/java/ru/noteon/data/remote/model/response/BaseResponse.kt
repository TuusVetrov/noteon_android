package ru.noteon.data.remote.model.response

enum class State {
    SUCCESS, NOT_FOUND, FAILED, UNAUTHORIZED
}

interface BaseResponse {
    val status: State
    val message: String
}
