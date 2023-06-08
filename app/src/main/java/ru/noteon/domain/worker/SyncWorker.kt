package ru.noteon.domain.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import ru.noteon.domain.task.TaskManager
import ru.noteon.domain.task.TaskState
import ru.noteon.core.utils.Either
import ru.noteon.data.repository.LocalNoteRepository
import ru.noteon.data.repository.RemoteNoteRepository
import ru.noteon.domain.model.NoteModel
import java.util.*

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localNoteRepository: LocalNoteRepository,
    private val remoteNoteRepository: RemoteNoteRepository,
    private val taskManager: TaskManager,
): CoroutineWorker(appContext, workerParams){

    override suspend fun doWork(): Result {
        return syncNotes()
    }

    private suspend fun syncNotes(): Result {
        return try {
            // Fetches all notes from remote.
            // If task of any note is still pending, skip it.
            val notes = fetchRemoteNotes().filter { note -> shouldReplaceNote(note.id) }

            // Add/Replace notes locally.
            localNoteRepository.addNotes(notes)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun fetchRemoteNotes(): List<NoteModel> {
        return when (val response = remoteNoteRepository.getAllNotes().first()) {
            is Either.Success -> response.data
            is Either.Error -> throw Exception(response.message)
        }
    }

    private fun shouldReplaceNote(noteId: String): Boolean {
        val taskId = taskManager.getTaskIdFromNoteId(noteId).toUUID()
        val state = taskManager.getTaskState(taskId)

        return (state == null || state != TaskState.SCHEDULED)
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}