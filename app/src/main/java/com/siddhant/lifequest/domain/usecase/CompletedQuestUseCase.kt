package com.siddhant.lifequest.domain.usecase;

import com.siddhant.lifequest.domain.repository.QuestRepository;
class CompletedQuestUseCase (
    private val repository: QuestRepository
){
    suspend operator fun invoke(id:Int){
        repository.completeQuest(id)
    }

}

