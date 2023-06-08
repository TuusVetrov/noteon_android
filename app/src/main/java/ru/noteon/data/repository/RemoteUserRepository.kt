package ru.noteon.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import ru.noteon.core.utils.Either
import ru.noteon.data.remote.api.AuthService
import ru.noteon.data.remote.api.UserService
import ru.noteon.data.remote.model.request.*
import ru.noteon.data.remote.model.response.State
import ru.noteon.data.remote.util.getResponse
import ru.noteon.domain.model.AuthCredential
import ru.noteon.domain.model.FolderModel
import ru.noteon.domain.model.UserModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteUserRepository @Inject internal constructor(
    private val authService: AuthService,
    private val userService: UserService
) {
    suspend fun registration(
        username: String,
        email: String,
        password: String
    ): Either<AuthCredential> {
        return runCatching {
            val authResponse = authService.register(RegistrationRequest(username, email, password)).getResponse()

            when (authResponse.status) {
                State.SUCCESS -> Either.success(AuthCredential(
                    authResponse.accessToken!!,
                    authResponse.refreshToken!!
                ))
                else -> Either.error(authResponse.message)
            }
        }.getOrDefault(Either.error("Что-то пошло не так!"))
    }

    suspend fun login(
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
        }.getOrDefault(Either.error("Что-то пошло не так!"))
    }


    fun getUser(): Flow<Either<UserModel>> = flow {
        val userResponse = userService.getUser().getResponse()

        val state = when (userResponse.status) {
            State.SUCCESS -> Either.success(
                UserModel(
                    userResponse.id,
                    userResponse.username,
                    userResponse.email
                )
            )
            else -> Either.error(userResponse.message)
        }
        emit(state)
    }.catch { emit(Either.error("Не удается синхронизировать последние пользовательские данные")) }

    suspend fun updateUserData(
        username: String,
        email: String
    ): Either<String> {
        return runCatching {
            val userResponse = userService.updateUserData(
                ChangeUserDataRequest(username, email)
            ).getResponse()

            when (userResponse.status) {
                State.SUCCESS -> Either.success(userResponse.id!!)
                else -> Either.error(userResponse.message)
            }
        }.getOrDefault(Either.error("Что-то пошло не так!"))
    }

    suspend fun changePassword(
        password: String,
    ): Either<String> {
        return runCatching {
            val userResponse = userService.updateUserPassword(
                ChangeUserPasswordRequest(password)
            ).getResponse()

            when (userResponse.status) {
                State.SUCCESS -> Either.success(userResponse.id!!)
                else -> Either.error(userResponse.message)
            }
        }.getOrDefault(Either.error("Что-то пошло не так!"))
    }
}