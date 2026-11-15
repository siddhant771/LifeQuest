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

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val getQuests: GetQuestsUseCase,
    private val addQuestUseCase: AddQuestUseCase,
    private val completeQuestUseCase: CompletedQuestUseCase,
    private val userPreferences: UserPreferences,
    private val questDao: QuestDao,
    private val remoteQuests: RemoteQuestRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests = _quests.asStateFlow()
    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter = _selectedFilter.asStateFlow()
    private val _uiState = MutableStateFlow(QuestUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private val syncMutex = Mutex()

    init {
        loadQuests()
        observeSelectedFilter()
        syncPending()
    }

    fun loadQuests() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = QuestUiState(isLoading = true)
            try {
                getQuests().collect { list ->
                    _quests.value = list
                    _uiState.value = QuestUiState()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = QuestUiState(errorMessage = "Unable to load quests. Please try again.")
            }
        }
    }

    fun observeSelectedFilter() {
        viewModelScope.launch {
            userPreferences.selectedFilter.collect { _selectedFilter.value = it }
        }
    }

    fun updateSelectedFilter(filter: String) {
        viewModelScope.launch { userPreferences.saveSelectedFilter(filter) }
    }

    fun addQuest(title: String, xp: Int, difficulty: Difficulty) {
        viewModelScope.launch {
            try {
                val nextId = (questDao.getAllOnce().maxOfOrNull { it.id } ?: 0) + 1
                addQuestUseCase(Quest(id = nextId, title = title, xp = xp, difficulty = difficulty))
                syncPending()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = QuestUiState(errorMessage = "Quest saved locally. Sync will retry when you are online.")
            }
        }
    }

    fun completeQuest(id: Int) {
        viewModelScope.launch {
            try {
                val row = questDao.getAllOnce().firstOrNull { it.id == id }
                    ?: throw IllegalArgumentException("Quest not found")
                if (row.completed || row.pendingCompletion) return@launch
                if (row.remoteId == null) {
                    questDao.queueCompletion(id)
                    syncPending()
                } else {
                    // Do not award XP locally. The server response is the source of truth.
                    remoteQuests.completeQuest(row.remoteId)
                    completeQuestUseCase.invoke(id)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = QuestUiState(errorMessage = "Completion is awaiting server confirmation. Retry sync when online.")
            }
        }
    }

    fun syncPending() {
        viewModelScope.launch {
            syncMutex.withLock {
                if (tokenManager.token.first().isNullOrBlank()) return@withLock
                try {
                    // Upload local quests first and persist their server IDs.
                    questDao.getPendingCreates().forEach { local ->
                        val remote = remoteQuests.createQuest(local.title, local.xp, local.difficulty)
                        questDao.attachRemoteId(local.id, remote.id)
                    }
                    // Completion requests are sent only after a server ID exists.
                    questDao.getPendingCompletions().forEach { local ->
                        val remoteId = local.remoteId ?: return@forEach
                        remoteQuests.completeQuest(remoteId)
                        questDao.markCompleted(local.id)
                    }
                    // Refresh server-owned completion state without replacing local primary keys.
                    val remoteRows = remoteQuests.getQuests()
                    val localRows = questDao.getAllOnce()
                    remoteRows.forEach { remote ->
                        val existing = localRows.firstOrNull { it.remoteId == remote.id }
                        val localId = existing?.id ?: ((questDao.getAllOnce().maxOfOrNull { it.id } ?: 0) + 1)
                        val difficulty = runCatching { Difficulty.valueOf(remote.difficulty.uppercase()) }
                            .getOrDefault(Difficulty.EASY)
                        questDao.insertQuest(
                            QuestEntity(
                                id = localId,
                                title = remote.title,
                                xp = remote.xp,
                                difficulty = difficulty.name,
                                completed = remote.completed,
                                remoteId = remote.id,
                                pendingCreate = false,
                                pendingCompletion = false
                            )
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.value = QuestUiState(errorMessage = "Some changes are still pending sync. They will be retried.")
                }
            }
        }
    }
}
