package com.siddhant.lifequest.data.repository

import com.siddhant.lifequest.data.local.QuestDao
import com.siddhant.lifequest.data.local.QuestEntity
import com.siddhant.lifequest.domain.model.Difficulty
import com.siddhant.lifequest.domain.model.Quest
import com.siddhant.lifequest.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val questDao: QuestDao
) : QuestRepository {

    override fun getQuests(): Flow<List<Quest>> {

        return questDao.getAllQuests().map { entities ->

            entities.map { entity ->

                Quest(
                    id = entity.id,
                    title = entity.title,
                    xp = entity.xp,
                    difficulty = Difficulty.valueOf(entity.difficulty),
                    completed = entity.completed
                )
            }
        }
    }

    override suspend fun addQuest(quest: Quest) {

        questDao.insertQuest(
            QuestEntity(
                id = quest.id,
                title = quest.title,
                xp = quest.xp,
                difficulty = quest.difficulty.name,
                completed = quest.completed
            )
        )
    }

    override suspend fun completeQuest(id: Int) {

        questDao.completeQuest(id)
    }
}