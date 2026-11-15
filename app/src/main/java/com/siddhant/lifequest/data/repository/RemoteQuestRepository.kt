package com.siddhant.lifequest.data.repository
import com.siddhant.lifequest.data.remote.api.QuestApi
import com.siddhant.lifequest.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton
@Singleton class RemoteQuestRepository @Inject constructor(private val api:QuestApi){
 suspend fun getQuests():List<QuestResponse> = api.getQuests()
 suspend fun createQuest(title:String,xp:Int,difficulty:String):QuestResponse=api.createQuest(CreateQuestRequest(title,xp,difficulty))
 suspend fun completeQuest(questId:Long):QuestCompletionResponse=api.completeQuest(questId)
}
