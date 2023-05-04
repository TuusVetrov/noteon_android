package ru.noteon.data.repository

import kotlinx.coroutines.flow.*
import ru.noteon.core.utils.Either
import ru.noteon.data.local.dao.FolderDao
import ru.noteon.data.local.entity.FolderEntity
import ru.noteon.domain.model.FolderModel
import java.util.*
import javax.inject.Inject

class LocalFolderRepository@Inject constructor(
    private val foldersDao: FolderDao
) {
    companion object {
        private const val PREFIX_TEMP_FOLDERS_ID = "TMP_FOLDER"
        fun generateTemporaryId() = "$PREFIX_TEMP_FOLDERS_ID-${UUID.randomUUID()}"
    }

    fun isTemporaryFolder(folderId: String) = folderId.startsWith(PREFIX_TEMP_FOLDERS_ID)

    fun getFolderById(folderId: String): Flow<FolderModel> = foldersDao.getFolderById(folderId)
        .filterNotNull()
        .map { FolderModel(it.id, it.folderName) }

    fun getAllFolders(): Flow<Either<List<FolderModel>>> = foldersDao.getAllFolders()
        .map { folders -> folders.map { FolderModel(it.id, it.folderName) } }
        .transform { folders -> emit(Either.success(folders)) }
        .catch { emit(Either.success(emptyList())) }

    fun searchFolder(searchQuery: String): Flow<List<FolderModel>> = foldersDao.searchFolder(searchQuery)
        .map { folders -> folders.map { FolderModel(it.id, it.folderName) } }

    suspend fun addFolder(
        folderName: String,
    ): Either<String> = runCatching {
        val tmpFolderId = generateTemporaryId()
        foldersDao.addFolder(
            FolderEntity(
                id = tmpFolderId,
                folderName = folderName,
            )
        )
        Either.success(tmpFolderId)
    }.getOrDefault(Either.error("Unable to create a new note"))

    suspend fun addFolders(folders: List<FolderModel>) = folders.map {
        FolderEntity(it.id, it.folderName)
    }.let {
        foldersDao.addFolders(it)
    }

    suspend fun updateFolder(
        folderId: String,
        folderName: String
    ): Either<String> = runCatching {
        foldersDao.updateFolderById(folderId, folderName)
        Either.success(folderId)
    }.getOrDefault(Either.error("Unable to update a note"))

    suspend fun deleteFolder(noteId: String): Either<String> = runCatching {
        foldersDao.deleteFolderById(noteId)
        Either.success(noteId)
    }.getOrDefault(Either.error("Unable to delete a note"))

    suspend fun updateFolderId(oldNoteId: String, newNoteId: String) =
        foldersDao.updateFolderId(oldNoteId, newNoteId)
}