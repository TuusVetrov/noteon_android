package ru.noteon.core.task

import androidx.lifecycle.asFlow
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import ru.noteon.core.utils.extensions.putEnum
import ru.noteon.core.worker.FolderSyncWorker
import ru.noteon.core.worker.FolderTaskWorker
import ru.noteon.core.worker.TaskWorker
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderTaskManager @Inject constructor(
    private val workManager: WorkManager
) {
    fun syncFolders(): UUID {
        val folderSyncWorker = OneTimeWorkRequestBuilder<FolderSyncWorker>()
            .setConstraints(getRequiredConstraints())
            .build()

        workManager.enqueueUniqueWork(
            SYNC_FOLDER_TASK_NAME,
            ExistingWorkPolicy.REPLACE,
            folderSyncWorker
        )
        return folderSyncWorker.id
    }


    fun scheduleTask(folderTask: FolderTask): UUID {
        val taskWorker = OneTimeWorkRequestBuilder<FolderTaskWorker>()
            .setConstraints(getRequiredConstraints())
            .setInputData(generateData(folderTask))
            .build()

        workManager.enqueueUniqueWork(
            getTaskIdFromFolderId(folderTask.folderId),
            ExistingWorkPolicy.REPLACE,
            taskWorker
        )

        return taskWorker.id
    }

    fun getTaskState(taskId: UUID): TaskState? = runCatching {
        workManager.getWorkInfoById(taskId)
            .get()
            .let { mapWorkInfoStateToTaskState(it.state) }
    }.getOrNull()

    fun observeTask(taskId: UUID): Flow<TaskState> {
        return workManager.getWorkInfoByIdLiveData(taskId)
            .asFlow()
            .map { mapWorkInfoStateToTaskState(it.state) }
            .transformWhile { taskState ->
                emit(taskState)

                // This is to terminate this flow when terminal state is arrived
                !taskState.isTerminalState
            }.distinctUntilChanged()
    }

    fun abortAllTasks() {
        workManager.cancelAllWork()
    }

    private fun mapWorkInfoStateToTaskState(state: WorkInfo.State): TaskState = when (state) {
        WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED -> TaskState.SCHEDULED
        WorkInfo.State.CANCELLED -> TaskState.CANCELLED
        WorkInfo.State.FAILED -> TaskState.FAILED
        WorkInfo.State.SUCCEEDED -> TaskState.COMPLETED
    }


    private fun generateData(folderTask: FolderTask) = Data.Builder()
        .putString(FolderTaskWorker.KEY_FOLDER_ID, folderTask.folderId)
        .putEnum(FolderTaskWorker.KEY_TASK_TYPE, folderTask.action)
        .build()

    private fun getRequiredConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun getTaskIdFromFolderId(folderId: String) = folderId

    companion object {
        const val SYNC_FOLDER_TASK_NAME = "Task-Folder-Sync"
    }
}