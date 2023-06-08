package ru.noteon.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import ru.noteon.data.remote.model.request.ChangeUserDataRequest
import ru.noteon.data.remote.model.request.ChangeUserPasswordRequest
import ru.noteon.data.remote.model.response.GetUserResponse
import ru.noteon.data.remote.model.response.UserDataChangeResponse

interface UserService {
    @GET("user")
    suspend fun getUser(): Response<GetUserResponse>

    @PUT("user/change_data")
    suspend fun updateUserData(
        @Body request: ChangeUserDataRequest
    ): Response<UserDataChangeResponse>

    @PUT("user/change_password")
    suspend fun updateUserPassword(
        @Body request: ChangeUserPasswordRequest
    ): Response<UserDataChangeResponse>
}