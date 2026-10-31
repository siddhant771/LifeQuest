package com.siddhant.lifequest.domain.usecase

import com.siddhant.lifequest.domain.repository.QuestRepository
import javax.inject.Inject
class GetQuestsUseCase @Inject constructor(
    private val repository: QuestRepository
) {
    operator fun invoke() = repository.getQuests()


}