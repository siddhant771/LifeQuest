package com.siddhant.lifequest.data.remote.dto
import com.google.gson.annotations.SerializedName
data class ProfileResponse(val userId:Long,val name:String,val email:String,val totalXp:Long,val currentStreak:Int,val longestStreak:Int)
data class LeaderboardEntry(@SerializedName("user_id") val userId:Long,val name:String,@SerializedName("weekly_xp") val weeklyXp:Long)
data class AchievementResponse(val id:Long,val code:String,val unlockedAt:String)
data class FriendChallengeRequest(val inviteeId:Long,val title:String,val targetXp:Int)
data class ChallengeCreatedResponse(val challengeId:Long,val status:String)
data class TeamChallengeRequest(val title:String,val targetXp:Int)
data class TeamChallengeResponse(val teamChallengeId:Long,val weekStart:String?=null)
