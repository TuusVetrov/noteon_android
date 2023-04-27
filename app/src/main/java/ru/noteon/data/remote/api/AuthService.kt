package ru.noteon.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import ru.noteon.data.remote.model.request.AuthRequest
import ru.noteon.data.remote.model.response.AuthResponse

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body authRequest: AuthRequest): Response<AuthResponse>

    @GET("auth/refaresh-token")
    suspend fun refreshToken(@Header("Authorization") token: String): Response<AuthResponse>
}