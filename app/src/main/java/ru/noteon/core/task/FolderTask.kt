package ru.noteon.core.task

class FolderTask private constructor(val folderId: String, val action: FolderTaskAction) {
    companion object {
        fun create(folderId: String) = FolderTask(folderId, FolderTaskAction.CREATE)
        fun update(folderId: String) = FolderTask(folderId, FolderTaskAction.UPDATE)
        fun delete(folderId: String) = FolderTask(folderId, FolderTaskAction.DELETE)
    }
}