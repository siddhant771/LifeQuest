package com.siddhant.lifequest.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.repository.AuthRepository
import com.siddhant.lifequest.data.repository.RemoteLifeQuestRepository
import com.siddhant.lifequest.data.remote.dto.ProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthUiState(val isLoading:Boolean=false,val isAuthenticated:Boolean=false,val isRegisterMode:Boolean=false,val message:String?=null,val profile:ProfileResponse?=null)
@HiltViewModel class AuthViewModel @Inject constructor(
 private val authRepository:AuthRepository,private val tokenManager:TokenManager,private val remote:RemoteLifeQuestRepository
):ViewModel(){
 private val _state=MutableStateFlow(AuthUiState(isLoading=true));val state=_state.asStateFlow()
 fun checkSession()=viewModelScope.launch{
  val token=tokenManager.token.first()
  if(token.isNullOrBlank()){_state.value=AuthUiState()}else{
   _state.value=AuthUiState(isLoading=true,isAuthenticated=true)
   runCatching{remote.profile()}.onSuccess{_state.value=AuthUiState(isAuthenticated=true,profile=it)}
    .onFailure{_state.value=AuthUiState(isAuthenticated=true,message="Offline mode · profile will refresh when connected")}
  }
 }
 fun setRegisterMode(enabled:Boolean){_state.value=_state.value.copy(isRegisterMode=enabled,message=null)}
 fun register(name:String,email:String,password:String)=viewModelScope.launch{
  _state.value=_state.value.copy(isLoading=true,message=null)
  runCatching{authRepository.register(name.trim(),email.trim(),password)}
   .onSuccess{_state.value=AuthUiState(message="Account created. Sign in to continue.")}
   .onFailure{_state.value=AuthUiState(message=it.message?.take(180)?: "Registration failed")}
 }
 fun login(email:String,password:String)=viewModelScope.launch{
  _state.value=AuthUiState(isLoading=true)
  runCatching{authRepository.login(email.trim(),password)}
   .onSuccess{val p=runCatching{remote.profile()}.getOrNull();_state.value=AuthUiState(isAuthenticated=true,profile=p,message=if(p==null)"Signed in · profile will sync when online" else null)}
   .onFailure{_state.value=AuthUiState(message=it.message?.take(180)?: "Login failed")}
 }
 fun refreshProfile()=viewModelScope.launch{runCatching{remote.profile()}.onSuccess{_state.value=_state.value.copy(profile=it)}}
 fun logout()=viewModelScope.launch{authRepository.logout();_state.value=AuthUiState()}
}
