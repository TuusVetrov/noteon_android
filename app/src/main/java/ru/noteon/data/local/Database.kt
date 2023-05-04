package ru.noteon.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.noteon.data.local.dao.FolderDao
import ru.noteon.data.local.dao.NotesDao
import ru.noteon.data.local.entity.FolderEntity
import ru.noteon.data.local.entity.NoteEntity

@androidx.room.Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 1
)
abstract class Database: RoomDatabase() {
    abstract fun getNotesDao(): NotesDao

    abstract fun getFoldersDao(): FolderDao

    companion object {
        private const val DB_NAME = "noteon_database"

        @Volatile
        private var INSTANCE: Database? = null

        fun getInstance(context: Context): Database {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Database::class.java,
                    DB_NAME
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}