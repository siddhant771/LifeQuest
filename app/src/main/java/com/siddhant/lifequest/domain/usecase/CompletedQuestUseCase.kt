package com.siddhant.lifequest.domain.usecase;
import javax.inject.Inject
import com.siddhant.lifequest.domain.repository.QuestRepository;
class CompletedQuestUseCase @Inject constructor (
    private val repository: QuestRepository
){
    suspend operator fun invoke(id:Int){
        repository.completeQuest(id)
    }

}

