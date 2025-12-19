package com.teambind.bind_android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bind_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys
    private object PreferencesKeys {
        val USER_ID = longPreferencesKey("user_id")
        val HAS_PROFILE = booleanPreferencesKey("has_profile")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
    }

    // User ID
    val userIdFlow: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID]
    }

    // Sync property for userId (blocking)
    var userId: String?
        get() = runBlocking { userIdFlow.first()?.toString() }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                if (value != null) {
                    preferences[PreferencesKeys.USER_ID] = value.toLong()
                } else {
                    preferences.remove(PreferencesKeys.USER_ID)
                }
            }
        }

    suspend fun getUserId(): Long? = userIdFlow.first()

    suspend fun setUserId(userId: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    // Has Profile
    val hasProfileFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_PROFILE] ?: false
    }

    // Sync property for hasProfile (blocking)
    var hasProfile: Boolean
        get() = runBlocking { hasProfileFlow.first() }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.HAS_PROFILE] = value
            }
        }

    suspend fun getHasProfile(): Boolean = hasProfileFlow.first()

    suspend fun setHasProfile(hasProfile: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_PROFILE] = hasProfile
        }
    }

    // Is Logged In
    val isLoggedInFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
    }

    suspend fun getIsLoggedIn(): Boolean = isLoggedInFlow.first()

    suspend fun setIsLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
        }
    }

    // User Name
    val userNameFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME]
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    // User Email
    val userEmailFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_EMAIL]
    }

    suspend fun setUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_EMAIL] = email
        }
    }

    // Profile Image URL
    val profileImageUrlFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PROFILE_IMAGE_URL]
    }

    suspend fun setProfileImageUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROFILE_IMAGE_URL] = url
        }
    }

    // Clear all data (logout)
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Save user info at once
    suspend fun saveUserInfo(
        userId: Long,
        userName: String? = null,
        userEmail: String? = null,
        profileImageUrl: String? = null
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            userName?.let { preferences[PreferencesKeys.USER_NAME] = it }
            userEmail?.let { preferences[PreferencesKeys.USER_EMAIL] = it }
            profileImageUrl?.let { preferences[PreferencesKeys.PROFILE_IMAGE_URL] = it }
        }
    }
}
