package ru.noteon.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.noteon.data.remote.model.request.FolderRequest
import ru.noteon.data.remote.model.response.FolderResponse
import ru.noteon.data.remote.model.response.GetAllFoldersResponse

interface FoldersService {
    @GET("folders")
    suspend fun getAllFolders(): Response<GetAllFoldersResponse>

    @POST("folder/new")
    suspend fun addFolder(@Body folderRequest: FolderRequest): Response<FolderResponse>

    @PUT("folder/{folderId}")
    suspend fun updateFolder(
        @Path("folderId") folderId: String,
        @Body folderRequest: FolderRequest
    ): Response<FolderResponse>

    @DELETE("folder/{folderId}")
    suspend fun deleteFolder(@Path("folderId") folderId: String): Response<FolderResponse>
}