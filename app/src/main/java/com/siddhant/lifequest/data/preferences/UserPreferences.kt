package com.siddhant.lifequest.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "user_preferences"
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val SELECTED_FILTER = stringPreferencesKey("selected_filter")
        private val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
    }

    val selectedFilter: Flow<String> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[SELECTED_FILTER] ?: "ALL"
            }

    val activeUserId: Flow<Long?> = context.dataStore.data.map { it[ACTIVE_USER_ID] }

    suspend fun saveActiveUserId(userId: Long) {
        context.dataStore.edit { it[ACTIVE_USER_ID] = userId }
    }

    suspend fun clearActiveUserId() {
        context.dataStore.edit { it.remove(ACTIVE_USER_ID) }
    }

    suspend fun saveSelectedFilter(filter: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_FILTER] = filter
        }
    }
}