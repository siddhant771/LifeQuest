package com.siddhant.lifequest.data.repository

import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.preferences.UserPreferences
import com.siddhant.lifequest.data.local.QuestDao
import kotlinx.coroutines.flow.first
import com.siddhant.lifequest.data.remote.api.AuthApi
import com.siddhant.lifequest.data.remote.dto.AuthResponse
import com.siddhant.lifequest.data.remote.dto.LoginRequest
import com.siddhant.lifequest.data.remote.dto.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val questDao: QuestDao,
    private val userPreferences: UserPreferences
) {
    suspend fun register(name: String, email: String, password: String): String {
        val response = authApi.register(RegisterRequest(name, email, password))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.errorBody()?.string() ?: "Registration failed")
        }
        return response.body()?.string().orEmpty()
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val result = authApi.login(LoginRequest(email, password))
        val previousUserId = userPreferences.activeUserId.first()
        // Local quests are account-scoped. Never expose one account's cached data
        // to another account on a shared device.
        if (previousUserId != result.userId) {
            questDao.deleteAll()
        }
        tokenManager.saveToken(result.token)
        userPreferences.saveActiveUserId(result.userId)
        return result
    }

    suspend fun clearExpiredSession() {
        // Keep the local cache for the same account so it can sync after re-authentication.
        tokenManager.clearToken()
    }

    suspend fun logout() {
        tokenManager.clearToken()
        questDao.deleteAll()
        userPreferences.clearActiveUserId()
    }
}
