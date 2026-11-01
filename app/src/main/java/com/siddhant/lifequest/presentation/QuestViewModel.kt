package com.siddhant.lifequest.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siddhant.lifequest.data.preferences.UserPreferences
import com.siddhant.lifequest.domain.model.Difficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.siddhant.lifequest.domain.model.Quest
import com.siddhant.lifequest.domain.usecase.AddQuestUseCase
import com.siddhant.lifequest.domain.usecase.CompletedQuestUseCase
import com.siddhant.lifequest.domain.usecase.GetQuestsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val getQuests: GetQuestsUseCase,
    private val addQuestUseCase: AddQuestUseCase,
    private val completeQuestUseCase: CompletedQuestUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // Holds the latest quest list
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests = _quests.asStateFlow()
    private val _selectedFilter = MutableStateFlow("ALL")

    val selectedFilter = _selectedFilter.asStateFlow()


    // Holds loading and error information
    private val _uiState = MutableStateFlow(QuestUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadQuests()
        observeSelectedFilter()
    }

    // Loads quests from Room through the repository and use case
    fun loadQuests() {

        loadJob?.cancel()

        loadJob = viewModelScope.launch {

            _uiState.value = QuestUiState(isLoading = true)

            try {

                getQuests().collect { questList ->

                    _quests.value = questList
                    _uiState.value = QuestUiState()

                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _uiState.value = QuestUiState(
                    errorMessage = "Unable to load quests. Please try again."
                )
            }
        }
    }
    fun observeSelectedFilter() {
        viewModelScope.launch {
            userPreferences.selectedFilter.collect { filter ->
                _selectedFilter.value = filter
            }
        }
    }
    fun updateSelectedFilter(filter: String) {
        viewModelScope.launch {
            userPreferences.saveSelectedFilter(filter)
        }
    }
    fun addQuest(title: String, xp: Int, difficulty: Difficulty) {
        viewModelScope.launch {
            try {
                val nextId = (_quests.value.maxOfOrNull { it.id } ?: 0) + 1

                val quest = Quest(
                    id = nextId,
                    title = title,
                    xp = xp,
                    difficulty = difficulty
                )

                addQuestUseCase(quest)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = QuestUiState(
                    errorMessage = "Unable to create quest. Please try again."
                )
            }
        }
    }

    // Completes a quest using the use case
    fun completeQuest(id: Int) {

        viewModelScope.launch {

            try {

                completeQuestUseCase.invoke(id)

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                _uiState.value = QuestUiState(
                    errorMessage = "Unable to complete quest. Please try again."
                )
            }
        }
    }
}