package ru.noteon.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import ru.noteon.core.task.TaskAction
import ru.noteon.core.utils.Either
import ru.noteon.core.utils.extensions.getEnum
import ru.noteon.data.repository.LocalNoteRepository
import ru.noteon.data.repository.RemoteNoteRepository
import ru.noteon.domain.model.NoteModel

@HiltWorker
class TaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val remoteNoteRepository: RemoteNoteRepository,
    private val localNoteRepository: LocalNoteRepository,
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RUN_ATTEMPTS)
            return Result.failure()

        val noteId = getNoteId()

        return when(getTaskAction()) {
            TaskAction.CREATE -> addNote(noteId)
            TaskAction.UPDATE -> updateNote(noteId)
            TaskAction.DELETE -> deleteNote(noteId)
            TaskAction.PIN -> pinNote(noteId)
        }
    }


    private suspend fun addNote(tempNoteId: String): Result {
        val note = fetchLocalNote(tempNoteId)
        val response = remoteNoteRepository.addNote(note.title, note.body)
        return if (response is Either.Success) {
            localNoteRepository.updateNoteId(tempNoteId, response.data)
            Result.success()
        } else {
            Result.retry()
        }
    }

    private suspend fun updateNote(noteId: String): Result {
        val note = fetchLocalNote(noteId)
        val response = remoteNoteRepository.updateNote(note.id, note.title, note.body)
        return if (response is Either.Success) Result.success() else Result.retry()
    }

    private suspend fun deleteNote(noteId: String): Result {
        val response = remoteNoteRepository.deleteNote(noteId)
        return if (response is Either.Success) Result.success() else Result.retry()
    }

    private suspend fun pinNote(noteId: String): Result {
        val note = fetchLocalNote(noteId)
        val response = remoteNoteRepository.pinNote(noteId, note.isPinned)
        return if (response is Either.Success) Result.success() else Result.retry()
    }

    private suspend fun fetchLocalNote(noteId: String): NoteModel =
        localNoteRepository.getNoteById(noteId).first()

    private fun getNoteId(): String = inputData.getString(KEY_NOTE_ID)
        ?: throw IllegalStateException("$KEY_NOTE_ID should be provided as input data.")

    private fun getTaskAction(): TaskAction = inputData.getEnum<TaskAction>(KEY_TASK_TYPE)
        ?: throw IllegalStateException("$KEY_TASK_TYPE should be provided as input data.")

    companion object {
        const val MAX_RUN_ATTEMPTS = 3
        const val KEY_NOTE_ID = "noteId"
        const val KEY_TASK_TYPE = "noty_task_type"
    }
}