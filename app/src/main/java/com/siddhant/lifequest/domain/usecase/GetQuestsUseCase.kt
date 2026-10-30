package com.siddhant.lifequest.domain.usecase

import com.siddhant.lifequest.domain.repository.QuestRepository

class GetQuestsUseCase(
    private val repository: QuestRepository
) {
    operator fun invoke() = repository.getQuests()


}