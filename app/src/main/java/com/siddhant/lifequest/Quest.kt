package com.siddhant.lifequest


// day2
enum class Difficulty(val multiplier:Int){
    EASY(1),
    MEDIUM(2),
    HARD(3)

}


data class Quest(
    val id: Int,
    val title: String,
    val xp: Int,
    val difficulty: Difficulty = Difficulty.EASY,
    val completed: Boolean = false
)

data class QuestState(
    val quests: List<Quest>,
    val totalXP: Int = 0
){
    val level : Int
        get() = totalXP/100+1
}
sealed class QuestResult {
    data class Success(val state: QuestState) : QuestResult()
    data object AlreadyCompleted : QuestResult()
    data object NotFound : QuestResult()
}


fun completeQuest(
    state: QuestState,
    id: Int
): QuestResult {

    val quest = state.quests.find { it.id == id }
        ?: return QuestResult.NotFound

    if (quest.completed) return QuestResult.AlreadyCompleted

    val reward = quest.xp * quest.difficulty.multiplier

    val updatedQuests = state.quests.map {
        if (it.id == id) it.copy(completed = true)
        else it
    }

    return QuestResult.Success(
        state.copy(
            quests = updatedQuests,
            totalXP = state.totalXP + reward
        )
    )
}
