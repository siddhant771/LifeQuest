package com.siddhant.lifequest.data.remote.api

import com.siddhant.lifequest.data.remote.dto.CreateQuestRequest
import com.siddhant.lifequest.data.remote.dto.QuestCompletionResponse
import com.siddhant.lifequest.data.remote.dto.QuestResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuestApi {
    @GET("api/quests")
    suspend fun getQuests(): List<QuestResponse>

    @POST("api/quests")
    suspend fun createQuest(@Body request: CreateQuestRequest): QuestResponse

    @POST("api/quests/{questId}/complete")
    suspend fun completeQuest(@Path("questId") questId: Long): QuestCompletionResponse
}
