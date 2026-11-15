package com.siddhant.lifequest.data.remote.api
import com.siddhant.lifequest.data.remote.dto.*
import retrofit2.http.*
interface LifeQuestApi {
 @GET("api/profile") suspend fun getProfile():ProfileResponse
 @GET("api/leaderboard/weekly") suspend fun getWeeklyLeaderboard():List<LeaderboardEntry>
 @GET("api/achievements") suspend fun getAchievements():List<AchievementResponse>
 @POST("api/challenges/friends") suspend fun inviteFriend(@Body request:FriendChallengeRequest):ChallengeCreatedResponse
 @POST("api/challenges/friends/{id}/accept") suspend fun acceptFriendInvite(@Path("id") id:Long):Map<String,Any>
 @POST("api/challenges/teams") suspend fun createTeam(@Body request:TeamChallengeRequest):TeamChallengeResponse
 @POST("api/challenges/teams/{id}/join") suspend fun joinTeam(@Path("id") id:Long):Map<String,Any>
}
