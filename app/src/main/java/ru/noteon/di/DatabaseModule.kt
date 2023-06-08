package ru.noteon.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.noteon.data.local.Database
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(application: Application) = Database.getInstance(application)

    @Singleton
    @Provides
    fun provideNotesDao(database: Database) = database.getNotesDao()

    @Singleton
    @Provides
    fun provideFoldersDao(database: Database) = database.getFoldersDao()

    @Singleton
    @Provides
    fun provideUserDao(database: Database) = database.getUserDao()
}