package com.siddhant.lifequest.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siddhant.lifequest.data.repository.AuthRepository
import com.siddhant.lifequest.data.repository.RemoteQuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(val isLoading:Boolean=false,val isAuthenticated:Boolean=false,val message:String?=null)

@HiltViewModel
class AuthViewModel @Inject constructor(
 private val authRepository:AuthRepository,
 private val remoteQuestRepository:RemoteQuestRepository
):ViewModel(){
 private val _state=MutableStateFlow(AuthUiState())
 val state=_state.asStateFlow()
 fun register(name:String,email:String,password:String)=viewModelScope.launch{
  _state.value=AuthUiState(isLoading=true)
  runCatching{authRepository.register(name,email,password)}.onSuccess{_state.value=AuthUiState(message=it)}.onFailure{_state.value=AuthUiState(message=it.message?: "Registration failed")}
 }
 fun login(email:String,password:String)=viewModelScope.launch{
  _state.value=AuthUiState(isLoading=true)
  runCatching{authRepository.login(email,password)}.onSuccess{
   _state.value=AuthUiState(isAuthenticated=true,message="Signed in")
  }.onFailure{_state.value=AuthUiState(message=it.message?: "Login failed")}
 }
 fun logout()=viewModelScope.launch{authRepository.logout();_state.value=AuthUiState(message="Signed out")}
}
