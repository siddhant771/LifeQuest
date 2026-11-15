package com.siddhant.lifequest.data.remote.dto
import com.google.gson.annotations.SerializedName
data class ProfileResponse(val userId:Long,val name:String,val email:String,val totalXp:Long,val currentStreak:Int,val longestStreak:Int)
data class LeaderboardEntry(val userId:Long,val name:String,val weeklyXp:Long)
data class AchievementResponse(val id:Long,val code:String,val unlockedAt:String)
data class FriendChallengeRequest(val inviteeId:Long,val title:String,val targetXp:Int)
data class FriendInvitation(val id:Long,@SerializedName("creator_id") val creatorId:Long,val title:String,@SerializedName("target_xp") val targetXp:Int,@SerializedName("starts_at") val startsAt:String,@SerializedName("ends_at") val endsAt:String,val status:String)
data class ChallengeCreatedResponse(val challengeId:Long,val status:String)
data class TeamChallengeRequest(val title:String,val targetXp:Int)
data class TeamChallengeResponse(val teamChallengeId:Long,val weekStart:String?=null)
data class TeamSummary(val id:Long,val title:String,@SerializedName("target_xp") val targetXp:Int,@SerializedName("week_start") val weekStart:String,@SerializedName("member_count") val memberCount:Int,@SerializedName("is_member") val isMember:Boolean)
data class TeamLeaderboardEntry(@SerializedName("user_id") val userId:Long,val name:String,@SerializedName("earned_xp") val earnedXp:Long)
