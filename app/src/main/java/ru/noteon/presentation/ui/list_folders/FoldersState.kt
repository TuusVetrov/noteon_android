package ru.noteon.presentation.ui.list_folders

import ru.noteon.domain.model.FolderModel

data class FoldersState(
    val isLoading: Boolean,
    val folders: List<FolderModel>,
    val error: String?,
    val isUserLoggedIn: Boolean?,
    val isConnectivityAvailable: Boolean?
) {
    companion object {
        val init = FoldersState(
            isLoading = false,
            folders = emptyList(),
            error = null,
            isUserLoggedIn = null,
            isConnectivityAvailable = null
        )
    }
}