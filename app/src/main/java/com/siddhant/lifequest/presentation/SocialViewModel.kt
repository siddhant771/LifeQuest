package com.siddhant.lifequest.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siddhant.lifequest.data.remote.dto.*
import com.siddhant.lifequest.data.repository.RemoteLifeQuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
data class SocialUiState(val loading:Boolean=false,val profile:ProfileResponse?=null,val leaderboard:List<LeaderboardEntry> = emptyList(),val achievements:List<AchievementResponse> = emptyList(),val teams:List<TeamSummary> = emptyList(),val message:String?=null)
@HiltViewModel class SocialViewModel @Inject constructor(private val repo:RemoteLifeQuestRepository):ViewModel(){
 private val _state=MutableStateFlow(SocialUiState());val state=_state.asStateFlow()
 fun refresh(){viewModelScope.launch{_state.value=_state.value.copy(loading=true,message=null)
  try{val p=repo.profile();val l=repo.weeklyLeaderboard();val a=repo.achievements();val t=repo.teams();_state.value=SocialUiState(profile=p,leaderboard=l,achievements=a,teams=t)}
  catch(e:Exception){_state.value=_state.value.copy(loading=false,message=e.message?.take(150)?: "Unable to refresh community data")}}
 }
 fun invite(emailOrId:String,title:String,target:Int){viewModelScope.launch{runCatching{repo.inviteFriend(emailOrId.toLong(),title,target)}.onSuccess{_state.value=_state.value.copy(message="Invitation created · #${it.challengeId}")}.onFailure{_state.value=_state.value.copy(message=it.message?: "Invite failed")}}}
 fun createTeam(title:String,target:Int){viewModelScope.launch{runCatching{repo.createTeam(title,target)}.onSuccess{refresh()}.onFailure{_state.value=_state.value.copy(message=it.message?: "Could not create league")}}}
 fun joinTeam(id:Long){viewModelScope.launch{runCatching{repo.joinTeam(id)}.onSuccess{refresh()}.onFailure{_state.value=_state.value.copy(message=it.message?: "Could not join league")}}}
}
