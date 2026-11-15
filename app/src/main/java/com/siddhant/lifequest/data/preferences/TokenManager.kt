package com.siddhant.lifequest.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tokenStore by preferencesDataStore(name = "auth_tokens")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tokenKey = stringPreferencesKey("access_token")
    val token: Flow<String?> = context.tokenStore.data.map { it[tokenKey] }

    suspend fun saveToken(token: String) {
        context.tokenStore.edit { it[tokenKey] = token }
    }

    suspend fun clearToken() {
        context.tokenStore.edit { it.remove(tokenKey) }
    }
}
