package ru.noteon.di

import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.noteon.core.task.FolderTaskManager
import ru.noteon.core.task.TaskManager

@Module
@InstallIn(SingletonComponent::class)
class TaskManagerModule {
    @Provides
    fun taskManager(workManager: WorkManager): TaskManager {
        return TaskManager(workManager)
    }

    @Provides
    fun folderTaskManager(workManager: WorkManager): FolderTaskManager {
        return FolderTaskManager(workManager)
    }
}