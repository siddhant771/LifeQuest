package com.siddhant.lifequest.data.repository
import com.siddhant.lifequest.data.local.*
import com.siddhant.lifequest.domain.model.*
import com.siddhant.lifequest.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class QuestRepositoryImpl @Inject constructor(private val questDao:QuestDao):QuestRepository{
 override fun getQuests():Flow<List<Quest>> = questDao.getAllQuests().map{rows->rows.map{Quest(it.id,it.title,it.xp,Difficulty.valueOf(it.difficulty),it.completed,it.remoteId,it.pendingCompletion)}}
 override suspend fun addQuest(quest:Quest){questDao.insertQuest(QuestEntity(quest.id,quest.title,quest.xp,quest.difficulty.name,quest.completed,quest.remoteId,pendingCreate=true,pendingCompletion=quest.pendingCompletion))}
 override suspend fun completeQuest(id:Int){questDao.completeQuest(id)}
}
