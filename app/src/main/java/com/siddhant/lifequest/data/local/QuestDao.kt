package com.siddhant.lifequest.data.local
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao interface QuestDao {
 @Query("SELECT * FROM quests ORDER BY id") fun getAllQuests():Flow<List<QuestEntity>>
 @Query("SELECT * FROM quests ORDER BY id") suspend fun getAllOnce():List<QuestEntity>
 @Query("SELECT * FROM quests WHERE pendingCreate = 1 ORDER BY id") suspend fun getPendingCreates():List<QuestEntity>
 @Query("SELECT * FROM quests WHERE pendingCompletion = 1 AND remoteId IS NOT NULL ORDER BY id") suspend fun getPendingCompletions():List<QuestEntity>
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun insertQuest(quest:QuestEntity)
 @Query("UPDATE quests SET remoteId=:remoteId,pendingCreate=0 WHERE id=:localId") suspend fun attachRemoteId(localId:Int,remoteId:Long)
 @Query("UPDATE quests SET pendingCompletion=1 WHERE id=:localId") suspend fun queueCompletion(localId:Int)
 @Query("UPDATE quests SET completed=1,pendingCompletion=0 WHERE id=:localId") suspend fun markCompleted(localId:Int)
 @Query("UPDATE quests SET completed=1,pendingCompletion=0 WHERE id=:questId") suspend fun completeQuest(questId:Int)
}
