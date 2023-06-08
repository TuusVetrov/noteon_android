package ru.noteon.domain.task

import android.util.Log
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import ru.noteon.core.utils.extensions.putEnum
import ru.noteon.domain.worker.SyncWorker
import ru.noteon.domain.worker.TaskWorker
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskManager @Inject constructor(
    private val workManager: WorkManager
) {
    fun syncNotes(): UUID {
        val notySyncWorker = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(getRequiredConstraints())
            .build()

        workManager.enqueueUniqueWork(
            SYNC_TASK_NAME,
            ExistingWorkPolicy.REPLACE,
            notySyncWorker
        )

        return notySyncWorker.id
    }

    fun scheduleTask(noteTask: NoteTask): UUID {
        val taskWorker = OneTimeWorkRequestBuilder<TaskWorker>()
            .setConstraints(getRequiredConstraints())
            .setInputData(generateData(noteTask))
            .build()

        workManager.enqueueUniqueWork(
            getTaskIdFromNoteId(noteTask.noteId),
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

    private fun generateData(noteTask: NoteTask) = Data.Builder()
        .putString(TaskWorker.KEY_NOTE_ID, noteTask.noteId)
        .putEnum(TaskWorker.KEY_TASK_TYPE, noteTask.action)
        .build()

    private fun getRequiredConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun getTaskIdFromNoteId(noteId: String) = noteId

    companion object {
        const val SYNC_TASK_NAME = "Task-Note-Sync"
    }
}