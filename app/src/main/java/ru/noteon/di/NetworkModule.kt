package ru.noteon.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.noteon.core.utils.moshi
import ru.noteon.data.remote.Constants
import ru.noteon.data.remote.api.AuthService
import ru.noteon.data.remote.api.NoteService
import ru.noteon.data.remote.interceptor.AuthInterceptor
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    private val baseRetrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(Constants.API_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))

    private val okHttpClientBuilder: OkHttpClient.Builder =
        OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)

    @Provides
    fun provideNoteService(authInterceptor: AuthInterceptor): NoteService {
        return baseRetrofitBuilder
            .client(okHttpClientBuilder.addInterceptor(authInterceptor).build())
            .build()
            .create(NoteService::class.java)
    }

    @Provides
    fun provideNotyAuthService(): AuthService {
        return baseRetrofitBuilder
            .build()
            .create(AuthService::class.java)
    }
}