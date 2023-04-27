package ru.noteon.core.token

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenSharedPreferencesFactory {
    private const val FILE_NAME_TOKEN_PREF = "auth_shared_pref"

    fun tokenPreferences(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME_TOKEN_PREF,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}