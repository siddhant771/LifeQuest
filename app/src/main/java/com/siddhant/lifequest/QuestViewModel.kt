package com.siddhant.lifequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestViewModel : ViewModel() {
    private val repository = QuestRepository()
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests = _quests.asStateFlow()
    private val _uiState = MutableStateFlow(QuestUiState())
    val uiState = _uiState.asStateFlow()
    fun loadQuests(){
        viewModelScope.launch {
            _uiState.value= QuestUiState(isLoading = true)
            try{
                _quests.value=repository.loadQuests()
                _uiState.value= QuestUiState()
            }
            catch (e: CancellationException){
                throw e
            }
            catch (e: Exception){
                _uiState.value= QuestUiState(
                    errorMessage = "Unable to load posts,Please try again."
                )
            }
        }
    }

}