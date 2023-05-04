package ru.noteon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.noteon.data.local.entity.FolderEntity

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE id = :folderId")
    fun getFolderById(folderId: String): Flow<FolderEntity?>

    @Query("SELECT * FROM folders ORDER BY folderName ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE folderName LIKE :searchQuery")
    fun searchFolder(searchQuery: String): Flow<List<FolderEntity>>

    @Insert
    suspend fun addFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFolders(folders: List<FolderEntity>)

    @Query("UPDATE folders SET folderName = :folderName WHERE id = :folderId")
    suspend fun updateFolderById(folderId: String, folderName: String)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: String)

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()

    @Query("UPDATE folders SET id = :newFolderId WHERE id = :oldFolderId")
    suspend fun updateFolderId(oldFolderId: String, newFolderId: String)
}