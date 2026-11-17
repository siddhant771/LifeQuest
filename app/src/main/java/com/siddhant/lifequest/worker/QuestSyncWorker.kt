package com.siddhant.lifequest.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.siddhant.lifequest.data.local.QuestDao
import com.siddhant.lifequest.data.preferences.TokenManager
import com.siddhant.lifequest.data.repository.RemoteQuestRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuestSyncDependencies {
    fun questDao(): QuestDao
    fun tokenManager(): TokenManager
    fun remoteQuestRepository(): RemoteQuestRepository
}

class QuestSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dependencies = EntryPointAccessors.fromApplication(
            applicationContext,
            QuestSyncDependencies::class.java
        )
        val dao = dependencies.questDao()
        val tokens = dependencies.tokenManager()
        val remote = dependencies.remoteQuestRepository()

        if (tokens.token.first().isNullOrBlank()) return Result.success()

        return try {
            dao.getPendingCreates().forEach { local ->
                // Persist the idempotency key before the request so retries are safe.
                val requestId = local.clientRequestId ?: UUID.randomUUID().toString().also {
                    dao.setClientRequestId(local.id, it)
                }
                val created = remote.createQuest(
                    requestId,
                    local.title,
                    local.xp,
                    local.difficulty
                )
                dao.attachRemoteId(local.id, created.id)
            }

            dao.getPendingCompletions().forEach { local ->
                val remoteId = local.remoteId ?: return@forEach
                try {
                    remote.completeQuest(remoteId)
                    dao.markCompleted(local.id)
                } catch (error: retrofit2.HttpException) {
                    val alreadyCompleted = error.code() == 409 &&
                        remote.getQuests().any { it.id == remoteId && it.completed }
                    if (alreadyCompleted) {
                        dao.markCompleted(local.id)
                    } else {
                        throw error
                    }
                }
            }
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.retry()
        }
    }
}
