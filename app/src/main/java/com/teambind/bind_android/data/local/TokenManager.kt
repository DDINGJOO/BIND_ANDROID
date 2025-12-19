package com.teambind.bind_android.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var accessToken: String?
        get() = securePrefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = securePrefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = securePrefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = securePrefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var deviceId: String?
        get() = securePrefs.getString(KEY_DEVICE_ID, null)
        set(value) = securePrefs.edit().putString(KEY_DEVICE_ID, value).apply()

    fun saveAllTokens(accessToken: String, refreshToken: String, deviceId: String? = null) {
        securePrefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            deviceId?.let { putString(KEY_DEVICE_ID, it) }
            apply()
        }
    }

    fun clearAllTokens() {
        securePrefs.edit().clear().apply()
    }

    fun hasValidToken(): Boolean {
        return !accessToken.isNullOrEmpty()
    }

    companion object {
        private const val SECURE_PREFS_NAME = "bind_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_DEVICE_ID = "device_id"
    }
}
