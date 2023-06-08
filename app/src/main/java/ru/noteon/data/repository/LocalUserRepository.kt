package ru.noteon.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import ru.noteon.core.utils.Either
import ru.noteon.data.local.dao.UserDao
import ru.noteon.data.local.entity.FolderEntity
import ru.noteon.data.local.entity.UserEntity
import ru.noteon.domain.model.NoteModel
import ru.noteon.domain.model.UserModel
import java.util.*
import javax.inject.Inject

class LocalUserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun addUser(
        id: String,
        username: String,
        email: String,
    ): Either<String> = runCatching {
        userDao.addUser(
            UserEntity(
                id = id,
                username = username,
                email = email
            )
        )
        Either.success(id)
    }.getOrDefault(Either.error("Не удалось добавить пользователя"))

    fun getUserById(userId: String): Flow<UserModel> = userDao.getUserById(userId)
        .filterNotNull()
        .map { UserModel(it.id, it.username, it.email) }

    suspend fun updateUser(
        id: String,
        username: String,
        email: String,
    ): Either<String> = runCatching {
        userDao.updateUserById(id, username, email)
        Either.success(id)
    }.getOrDefault(Either.error("Не удалось обновить пользователя"))


}