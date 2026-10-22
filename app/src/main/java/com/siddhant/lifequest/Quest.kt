package com.siddhant.lifequest




data class Quest(
    val id: Int,
    val title: String,
    val xp: Int,
    val completed: Boolean = false
)

data class QuestState(
    val quests: List<Quest>,
    val totalXP: Int = 0
)

fun completeQuest(
    state: QuestState,
    id: Int
): QuestState {

    val quest = state.quests.find { it.id == id }
        ?: return state

    if (quest.completed) return state

    val updatedQuests = state.quests.map {
        if (it.id == id) {
            it.copy(completed = true)
        } else {
            it
        }
    }

    return state.copy(
        quests = updatedQuests,
        totalXP = state.totalXP + quest.xp
    )
}
