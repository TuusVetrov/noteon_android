package ru.noteon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.noteon.data.local.entity.NoteEntity
import ru.noteon.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: UserEntity)

    @Query("UPDATE user SET username = :newUsername, email = :newEmail WHERE id = :userId")
    suspend fun updateUserById(userId: String, newUsername: String, newEmail: String)

    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>
}