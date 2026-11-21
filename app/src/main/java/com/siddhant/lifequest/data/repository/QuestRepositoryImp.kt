package com.siddhant.lifequest.data.repository
import com.siddhant.lifequest.data.local.*
import com.siddhant.lifequest.domain.model.*
import com.siddhant.lifequest.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class QuestRepositoryImpl @Inject constructor(private val dao:QuestDao):QuestRepository{
 override fun getQuests():Flow<List<Quest>> = dao.getAllQuests().map{rows->rows.map{Quest(it.id,it.title,it.xp,Difficulty.valueOf(it.difficulty),it.completed,it.remoteId,it.pendingCompletion,it.clientRequestId)}}
 override suspend fun addQuest(quest:Quest){dao.insertQuest(QuestEntity(quest.id,quest.title,quest.xp,quest.difficulty.name,quest.completed,quest.remoteId,true,quest.pendingCompletion,quest.clientRequestId))}
 override suspend fun completeQuest(id:Int){dao.queueCompletion(id)}
}
