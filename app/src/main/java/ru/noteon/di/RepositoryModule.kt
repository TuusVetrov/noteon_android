package ru.noteon.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.noteon.data.local.dao.NotesDao
import ru.noteon.data.remote.api.AuthService
import ru.noteon.data.remote.api.NoteService
import ru.noteon.data.repository.LocalNoteRepository
import ru.noteon.data.repository.RemoteNoteRepository
import ru.noteon.data.repository.UserRepository

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    fun authRepository(authService: AuthService): UserRepository {
        return UserRepository(authService)
    }

    @Provides
    fun localNoteRepository(
        notesDao: NotesDao,
    ): LocalNoteRepository {
        return LocalNoteRepository(notesDao)
    }

    @Provides
    fun remoteNoteRepository(noteService: NoteService): RemoteNoteRepository {
        return RemoteNoteRepository(noteService)
    }
}