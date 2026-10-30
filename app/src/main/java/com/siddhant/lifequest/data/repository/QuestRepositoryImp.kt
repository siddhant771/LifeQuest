package com.siddhant.lifequest.data.repository
import javax.inject.Inject
import com.siddhant.lifequest.domain.model.Quest
import com.siddhant.lifequest.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class QuestRepositoryImp @Inject constructor() : QuestRepository {
    private val _quests = MutableStateFlow(
        listOf(
            Quest(1, "Study Kotlin", 50),
            Quest(2,"Solve DSA Problems",50),
            Quest(3,"Workout",30)

        )
    )

    override fun getQuests(): Flow<List<Quest>> {
        return _quests.asStateFlow()
    }

    override suspend fun addQuest(quest: Quest) {
        _quests.update { current -> current+quest }

    }

    override suspend fun completeQuest(id: Int) {
        _quests.update { current ->
            current.map{quest ->
                if(quest.id == id && !quest.completed ){
                    quest.copy(completed = true)
                }
                else{
                    quest
                }
            }
        }
    }
}