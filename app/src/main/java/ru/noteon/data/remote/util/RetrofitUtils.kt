package ru.noteon.data.remote.util

import retrofit2.Response
import ru.noteon.core.utils.fromJson

inline fun <reified T> Response<T>.getResponse(): T {
    val responseBody = body()
    return if (this.isSuccessful && responseBody != null) {
        responseBody
    } else {
        fromJson<T>(errorBody()!!.string())!!
    }
}