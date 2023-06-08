package ru.noteon.core.token
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }

    fun saveTokens(accessToken: String?, refreshToken: String?) {
        sharedPreferences.edit(commit = true) {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
        }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }
}