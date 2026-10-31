package com.siddhant.lifequest.domain.usecase;


import com.siddhant.lifequest.domain.model.Quest
import com.siddhant.lifequest.domain.repository.QuestRepository
import javax.inject.Inject

class AddQuestUseCase @Inject constructor(
        private val repository: QuestRepository
) {
    suspend operator fun invoke(quest: Quest) {
        repository.addQuest(quest)
    }
}
