package com.siddhant.lifequest.domain.repository

import com.siddhant.lifequest.domain.model.Quest
import kotlinx.coroutines.flow.Flow


interface QuestRepository {

    fun getQuests(): Flow<List<Quest>>

    suspend fun addQuest(quest: Quest)

    suspend fun completeQuest(id: Int)
}