package com.siddhant.lifequest.data.repository

import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.remote.api.AuthApi
import com.siddhant.lifequest.data.remote.dto.AuthResponse
import com.siddhant.lifequest.data.remote.dto.LoginRequest
import com.siddhant.lifequest.data.remote.dto.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
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
        tokenManager.saveToken(result.token)
        return result
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}
