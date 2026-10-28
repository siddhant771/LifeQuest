package com.siddhant.lifequest

import android.os.Message

data class QuestUiState(
    val isLoading: Boolean=false,
    val errorMessage: String?=null
)