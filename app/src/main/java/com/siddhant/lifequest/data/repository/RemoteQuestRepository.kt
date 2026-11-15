package com.siddhant.lifequest.data.repository

import com.siddhant.lifequest.data.remote.api.QuestApi
import com.siddhant.lifequest.data.remote.dto.CreateQuestRequest
import com.siddhant.lifequest.data.remote.dto.QuestResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteQuestRepository @Inject constructor(
    private val questApi: QuestApi
) {
    suspend fun getQuests(): List<QuestResponse> = questApi.getQuests()

    suspend fun createQuest(title: String, xp: Int, difficulty: String): QuestResponse =
        questApi.createQuest(CreateQuestRequest(title, xp, difficulty))

    suspend fun completeQuest(questId: Long) = questApi.completeQuest(questId)
}
