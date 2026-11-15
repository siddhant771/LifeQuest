package com.siddhant.lifequest.data.remote.dto
data class CreateQuestRequest(val title:String,val xp:Int,val difficulty:String,val clientRequestId:String?=null)
data class QuestResponse(val id:Long,val userId:Long,val title:String,val xp:Int,val difficulty:String,val completed:Boolean)
data class QuestCompletionResponse(val eventId:Long,val userId:Long,val questId:Long,val xpAwarded:Int,val completedAt:String)
