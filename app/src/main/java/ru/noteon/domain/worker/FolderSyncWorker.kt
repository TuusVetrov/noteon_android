package ru.noteon.domain.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import ru.noteon.domain.task.FolderTaskManager
import ru.noteon.domain.task.TaskState
import ru.noteon.core.utils.Either
import ru.noteon.data.repository.LocalFolderRepository
import ru.noteon.data.repository.RemoteFolderRepository
import ru.noteon.domain.model.FolderModel
import ru.noteon.domain.model.NoteModel
import java.util.*

@HiltWorker
class FolderSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localFolderRepository: LocalFolderRepository,
    private val remoteFolderRepository: RemoteFolderRepository,
    private val folderTaskManager: FolderTaskManager
): CoroutineWorker(appContext, workerParams){

    override suspend fun doWork(): Result {
       return syncFolders()
    }

    private suspend fun syncFolders(): Result {
        return try {
            // Fetches all folders from remote.
            // If task of any note is still pending, skip it.
            val folders = fetchRemoteFolders().filter { folder -> shouldReplaceFolder(folder.id) }

            // Add/Replace notes locally.
            localFolderRepository.addFolders(folders)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun fetchRemoteFolders(): List<FolderModel> {
        return when (val response = remoteFolderRepository.getAllFolders().first()) {
            is Either.Success -> response.data
            is Either.Error -> throw Exception(response.message)
        }
    }

    private fun shouldReplaceFolder(folderId: String): Boolean {
        val taskId = folderTaskManager.getTaskIdFromFolderId(folderId).toUUID()
        val state = folderTaskManager.getTaskState(taskId)

        return (state == null || state != TaskState.SCHEDULED)
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}