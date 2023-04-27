package ru.noteon.di

import android.app.Application
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.noteon.core.connectivity.ConnectivityObserver
import ru.noteon.core.token.TokenManager
import ru.noteon.core.token.TokenSharedPreferencesFactory
import ru.noteon.core.utils.connectivityManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideTokenManager(application: Application): TokenManager {
        return TokenManager(TokenSharedPreferencesFactory.tokenPreferences(application))
    }

    @Singleton
    @Provides
    fun provideConnectivityObserver(application: Application): ConnectivityObserver {
        return ConnectivityObserver(application.connectivityManager)
    }

    @Singleton
    @Provides
    fun provideWorkManager(application: Application): WorkManager {
        return WorkManager.getInstance(application)
    }
}