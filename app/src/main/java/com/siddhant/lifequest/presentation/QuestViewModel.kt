
package com.siddhant.lifequest.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siddhant.lifequest.domain.model.Quest
import com.siddhant.lifequest.domain.usecase.CompletedQuestUseCase
import com.siddhant.lifequest.domain.usecase.GetQuestsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class QuestViewModel(
    private val getQuests: GetQuestsUseCase,
    private val completeQuest: CompletedQuestUseCase
) : ViewModel() {

    // Holds the latest quest list
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests = _quests.asStateFlow()

    // Holds loading and error information
    private val _uiState = MutableStateFlow(QuestUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    // Loads quests from the repository through the use case
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

    // Completes a quest using the use case
    fun completeQuest(id: Int) {
        viewModelScope.launch {
            try {
                completeQuest.invoke(id)
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
