package ru.noteon.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import ru.noteon.core.utils.Either
import ru.noteon.data.remote.api.FoldersService
import ru.noteon.data.remote.model.request.FolderRequest
import ru.noteon.data.remote.model.response.State
import ru.noteon.data.remote.util.getResponse
import ru.noteon.domain.model.FolderModel
import javax.inject.Singleton

@Singleton
class RemoteFolderRepository(
    private val folderService: FoldersService
) {
    fun getAllFolders(): Flow<Either<List<FolderModel>>> = flow {
        val foldersResponse = folderService.getAllFolders().getResponse()

        val state = when (foldersResponse.status) {
            State.SUCCESS -> Either.success(foldersResponse.folders)
            else -> Either.error(foldersResponse.message)
        }

        emit(state)
    }.catch { emit(Either.error("Не удается синхронизировать последние заметки")) }

    suspend fun addFolder(folderName: String): Either<String> {
        return runCatching {
            val folderResponse = folderService.addFolder(FolderRequest(folderName)).getResponse()

            when (folderResponse.status) {
                State.SUCCESS -> Either.success(folderResponse.folderId!!)
                else -> Either.error(folderResponse.message)
            }
        }.getOrElse {
            it.printStackTrace()
            (Either.error("Что-то пошло не так!"))
        }
    }

    suspend fun updateFolder(
        folderId: String,
        folderName: String,
    ): Either<String> {
        return runCatching {
            val foldersResponse = folderService.updateFolder(
                folderId,
                FolderRequest(folderName)
            ).getResponse()

            when (foldersResponse.status) {
                State.SUCCESS -> Either.success(foldersResponse.folderId!!)
                else -> Either.error(foldersResponse.message)
            }
        }.getOrDefault(Either.error("Что-то пошло не так!"))
    }

    suspend fun deleteFolder(folderId: String): Either<String> {
        return runCatching {
            val folderResponse = folderService.deleteFolder(folderId).getResponse()

            when (folderResponse.status) {
                State.SUCCESS -> Either.success(folderResponse.folderId!!)
                else -> Either.error(folderResponse.message)
            }
        }.getOrDefault(Either.error("Что-то пошло не так!"))
    }
}