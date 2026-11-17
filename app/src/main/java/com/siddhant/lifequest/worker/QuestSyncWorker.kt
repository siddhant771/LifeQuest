package com.siddhant.lifequest.worker
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.local.QuestDao
import com.siddhant.lifequest.data.repository.RemoteQuestRepository
import kotlinx.coroutines.CancellationException\nimport kotlinx.coroutines.flow.first\nimport java.util.UUID
@EntryPoint @InstallIn(SingletonComponent::class) interface QuestSyncDependencies{
 fun questDao():QuestDao
 fun tokenManager():TokenManager
 fun remoteQuestRepository():RemoteQuestRepository
}
class QuestSyncWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params){
 override suspend fun doWork():Result{
  val deps=EntryPointAccessors.fromApplication(applicationContext,QuestSyncDependencies::class.java)
  val dao=deps.questDao();val tokens=deps.tokenManager();val remote=deps.remoteQuestRepository()
  if(tokens.token.first().isNullOrBlank())return Result.success()
  return try{
   dao.getPendingCreates().forEach { local ->
    // Keep the same key across WorkManager retries and app restarts.
    val requestId = local.clientRequestId ?: UUID.randomUUID().toString().also {
     dao.setClientRequestId(local.id, it)
    }
    val created = remote.createQuest(requestId, local.title, local.xp, local.difficulty)
    dao.attachRemoteId(local.id, created.id)
   }
   dao.getPendingCompletions().forEach{local->val id=local.remoteId?:return@forEach
    try{remote.completeQuest(id);dao.markCompleted(local.id)}catch(e:retrofit2.HttpException){if(e.code()==409&&remote.getQuests().any{it.id==id&&it.completed})dao.markCompleted(local.id)else throw e}}
   Result.success()
  }catch(e:CancellationException){throw e}catch(e:Exception){Result.retry()}
 }
}
