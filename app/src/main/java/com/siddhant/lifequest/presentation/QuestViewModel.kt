package com.siddhant.lifequest.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siddhant.lifequest.data.local.QuestDao
import com.siddhant.lifequest.data.local.QuestEntity
import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.preferences.UserPreferences
import com.siddhant.lifequest.data.repository.RemoteQuestRepository
import com.siddhant.lifequest.domain.model.Difficulty
import com.siddhant.lifequest.domain.model.Quest
import com.siddhant.lifequest.domain.usecase.AddQuestUseCase
import com.siddhant.lifequest.domain.usecase.CompletedQuestUseCase
import com.siddhant.lifequest.domain.usecase.GetQuestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
@HiltViewModel class QuestViewModel @Inject constructor(
 private val getQuests:GetQuestsUseCase,private val addQuestUseCase:AddQuestUseCase,private val completeQuestUseCase:CompletedQuestUseCase,
 private val userPreferences:UserPreferences,private val questDao:QuestDao,private val remoteQuests:RemoteQuestRepository,private val tokenManager:TokenManager
):ViewModel(){
 private val _quests=MutableStateFlow<List<Quest>>(emptyList());val quests=_quests.asStateFlow()
 private val _selectedFilter=MutableStateFlow("ALL");val selectedFilter=_selectedFilter.asStateFlow()
 private val _uiState=MutableStateFlow(QuestUiState());val uiState=_uiState.asStateFlow()
 private var loadJob:Job?=null;private val mutex=Mutex()
 init{loadQuests();viewModelScope.launch{userPreferences.selectedFilter.collect{_selectedFilter.value=it}};syncPending()}
 fun loadQuests(){loadJob?.cancel();loadJob=viewModelScope.launch{_uiState.value=QuestUiState(isLoading=true);try{getQuests().collect{_quests.value=it;_uiState.value=QuestUiState()}}catch(e:CancellationException){throw e}catch(e:Exception){_uiState.value=QuestUiState(errorMessage="Unable to load quests")}}}
 fun updateSelectedFilter(filter:String){viewModelScope.launch{userPreferences.saveSelectedFilter(filter)}}
 fun addQuest(title:String,xp:Int,difficulty:Difficulty){viewModelScope.launch{try{val id=(questDao.getAllOnce().maxOfOrNull{it.id}?:0)+1;addQuestUseCase(Quest(id,title,xp,difficulty,clientRequestId=UUID.randomUUID().toString()));syncPending()}catch(e:CancellationException){throw e}catch(e:Exception){_uiState.value=QuestUiState(errorMessage="Quest saved locally; sync will retry")}}}
 fun completeQuest(id:Int){viewModelScope.launch{try{val row=questDao.getAllOnce().firstOrNull{it.id==id}?:return@launch;if(row.completed||row.pendingCompletion)return@launch;questDao.queueCompletion(id);syncPending()}catch(e:CancellationException){throw e}catch(e:Exception){_uiState.value=QuestUiState(errorMessage="Completion queued. Sync when online.")}}}
 fun syncPending(){viewModelScope.launch{mutex.withLock{if(tokenManager.token.first().isNullOrBlank())return@withLock
  try{
   questDao.getPendingCreates().forEach { local ->
    // Persist the idempotency key before the network call so retries after process death
    // cannot create a duplicate quest on the server.
    val requestId = local.clientRequestId ?: UUID.randomUUID().toString().also {
        questDao.setClientRequestId(local.id, it)
    }
    val remote = remoteQuests.createQuest(requestId, local.title, local.xp, local.difficulty)
    questDao.attachRemoteId(local.id, remote.id)
   }
   questDao.getPendingCompletions().forEach{local->val id=local.remoteId?:return@forEach
    try{remoteQuests.completeQuest(id);questDao.markCompleted(local.id)}
    catch(e:retrofit2.HttpException){if(e.code()==409&&remoteQuests.getQuests().any{it.id==id&&it.completed})questDao.markCompleted(local.id)else throw e}}
   var localRows=questDao.getAllOnce()
   remoteQuests.getQuests().forEach{remote->val existing=localRows.firstOrNull{it.remoteId==remote.id};val localId=existing?.id?:((localRows.maxOfOrNull{it.id}?:0)+1)
    val diff=runCatching{Difficulty.valueOf(remote.difficulty.uppercase())}.getOrDefault(Difficulty.EASY)
    questDao.insertQuest(QuestEntity(localId,remote.title,remote.xp,diff.name,remote.completed,remote.id,false,false));localRows=questDao.getAllOnce()}
   _uiState.value=QuestUiState()
  }catch(e:CancellationException){throw e}catch(e:Exception){_uiState.value=QuestUiState(errorMessage="Sync paused. Pending changes will retry when connected.")}
 }}}
}
