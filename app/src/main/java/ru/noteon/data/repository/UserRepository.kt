package ru.noteon.data.repository

import android.util.Log
import ru.noteon.core.utils.Either
import ru.noteon.data.remote.api.AuthService
import ru.noteon.data.remote.model.request.AuthRequest
import ru.noteon.data.remote.model.response.State
import ru.noteon.data.remote.util.getResponse
import ru.noteon.domain.model.AuthCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject internal constructor(
    private val authService: AuthService
) {
    suspend fun addUser(
        email: String,
        password: String
    ): Either<AuthCredential> {
        return runCatching {
            val authResponse = authService.register(AuthRequest(email, password)).getResponse()

            when (authResponse.status) {
                State.SUCCESS -> Either.success(AuthCredential(
                    authResponse.accessToken!!,
                    authResponse.refreshToken!!
                ))
                else -> Either.error(authResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }

    suspend fun getUserByEmailAndPassword(
        email: String,
        password: String
    ): Either<AuthCredential> {
        return runCatching {
            val authResponse = authService.login(AuthRequest(email, password)).getResponse()
            when (authResponse.status) {
                State.SUCCESS -> Either.success(AuthCredential(
                    authResponse.accessToken!!,
                    authResponse.refreshToken!!
                ))
                else -> Either.error(authResponse.message)
            }
        }.getOrDefault(Either.error("Something went wrong!"))
    }
}