package com.siddhant.lifequest.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.repository.AuthRepository
import com.siddhant.lifequest.data.repository.RemoteLifeQuestRepository
import com.siddhant.lifequest.data.remote.dto.ProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isRegisterMode: Boolean = false,
    val message: String? = null,
    val profile: ProfileResponse? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val remote: RemoteLifeQuestRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState(isLoading = true))
    val state = _state.asStateFlow()

    fun checkSession() = viewModelScope.launch {
        val token = tokenManager.token.first()
        if (token.isNullOrBlank()) {
            _state.value = AuthUiState()
            return@launch
        }

        _state.value = AuthUiState(isLoading = true, isAuthenticated = true)
        try {
            val profile = remote.profile()
            _state.value = AuthUiState(isAuthenticated = true, profile = profile)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: HttpException) {
            if (error.code() == 401) {
                authRepository.clearExpiredSession()
                _state.value = AuthUiState(message = "Session expired. Please sign in again.")
            } else {
                _state.value = AuthUiState(
                    isAuthenticated = true,
                    message = "Offline mode · profile will refresh when connected"
                )
            }
        } catch (error: Exception) {
            _state.value = AuthUiState(
                isAuthenticated = true,
                message = "Offline mode · profile will refresh when connected"
            )
        }
    }

    fun setRegisterMode(enabled: Boolean) {
        _state.value = _state.value.copy(isRegisterMode = enabled, message = null)
    }

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, message = null)
        try {
            authRepository.register(name.trim(), email.trim(), password)
            _state.value = AuthUiState(message = "Account created. Sign in to continue.")
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            _state.value = AuthUiState(message = error.message?.take(180) ?: "Registration failed")
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthUiState(isLoading = true)
        try {
            authRepository.login(email.trim(), password)
            val profile = try {
                remote.profile()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                null
            }
            _state.value = AuthUiState(
                isAuthenticated = true,
                profile = profile,
                message = if (profile == null) "Signed in · profile will sync when online" else null
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            _state.value = AuthUiState(message = error.message?.take(180) ?: "Login failed")
        }
    }

    fun refreshProfile() = viewModelScope.launch {
        try {
            val profile = remote.profile()
            _state.value = _state.value.copy(profile = profile, message = null)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: HttpException) {
            if (error.code() == 401) {
                authRepository.clearExpiredSession()
                _state.value = AuthUiState(message = "Session expired. Please sign in again.")
            }
        } catch (error: Exception) {
            _state.value = _state.value.copy(
                message = "Profile refresh failed. Check your connection."
            )
        }
    }

    fun logout() = viewModelScope.launch {
        authRepository.logout()
        _state.value = AuthUiState()
    }
}
