package com.siddhant.lifequest.data.repository
import com.siddhant.lifequest.data.remote.api.LifeQuestApi
import com.siddhant.lifequest.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton
@Singleton class RemoteLifeQuestRepository @Inject constructor(private val api:LifeQuestApi){
 suspend fun profile()=api.getProfile()
 suspend fun weeklyLeaderboard()=api.getWeeklyLeaderboard()
 suspend fun achievements()=api.getAchievements()
 suspend fun teams()=api.getTeams()
 suspend fun teamLeaderboard(id:Long)=api.getTeamLeaderboard(id)
 suspend fun invitations()=api.invitations()
 suspend fun inviteFriend(inviteeId:Long,title:String,targetXp:Int)=api.inviteFriend(FriendChallengeRequest(inviteeId,title,targetXp))
 suspend fun acceptInvite(id:Long)=api.acceptFriendInvite(id)
 suspend fun createTeam(title:String,targetXp:Int)=api.createTeam(TeamChallengeRequest(title,targetXp))
 suspend fun joinTeam(id:Long)=api.joinTeam(id)
}
