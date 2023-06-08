package ru.noteon.domain.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import ru.noteon.domain.task.FolderTaskAction
import ru.noteon.core.utils.Either
import ru.noteon.core.utils.extensions.getEnum
import ru.noteon.data.repository.LocalFolderRepository
import ru.noteon.data.repository.RemoteFolderRepository
import ru.noteon.domain.model.FolderModel

@HiltWorker
class FolderTaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val remoteFolderRepository: RemoteFolderRepository,
    private val localFolderRepository: LocalFolderRepository,
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RUN_ATTEMPTS)
            return Result.failure()

        val folderId = getFolderId()

        return when(getTaskAction()) {
            FolderTaskAction.CREATE -> addFolder(folderId)
            FolderTaskAction.UPDATE -> updateFolder(folderId)
            FolderTaskAction.DELETE -> deleteFolder(folderId)
        }
    }

    private suspend fun addFolder(tempFolderId: String): Result {
        val folder = fetchLocalFolder(tempFolderId)
        val response = remoteFolderRepository.addFolder(folder.folderName)
        return if (response is Either.Success) {
            localFolderRepository.updateFolderId(tempFolderId, response.data)
            Result.success()
        } else {
            Result.retry()
        }
    }

    private suspend fun updateFolder(folderId: String): Result {
        val folder = fetchLocalFolder(folderId)
        val response = remoteFolderRepository.updateFolder(folder.id, folder.folderName)
        return if (response is Either.Success) Result.success() else Result.retry()
    }

    private suspend fun deleteFolder(folderId: String): Result {
        val response = remoteFolderRepository.deleteFolder(folderId)
        return if (response is Either.Success) Result.success() else Result.retry()
    }

    private suspend fun fetchLocalFolder(folderId: String): FolderModel =
        localFolderRepository.getFolderById(folderId).first()

    private fun getFolderId(): String = inputData.getString(KEY_FOLDER_ID)
        ?: throw IllegalStateException("$KEY_FOLDER_ID should be provided as input data.")

    private fun getTaskAction(): FolderTaskAction = inputData.getEnum<FolderTaskAction>(
        KEY_TASK_TYPE
    )
        ?: throw IllegalStateException("${TaskWorker.KEY_TASK_TYPE} should be provided as input data.")

    companion object {
        const val MAX_RUN_ATTEMPTS = 3
        const val KEY_FOLDER_ID = "folderId"
        const val KEY_TASK_TYPE = "noty_task_type"
    }
}