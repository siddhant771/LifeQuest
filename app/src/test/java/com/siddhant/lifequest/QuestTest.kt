package com.siddhant.lifequest




import org.junit.Assert.*
import org.junit.Test

class QuestTest {

    private fun initialState() = QuestState(
        listOf(
            Quest(1, "Kotlin", 50, Difficulty.EASY),
            Quest(2, "DSA", 50, Difficulty.MEDIUM),
            Quest(3, "Workout", 30, Difficulty.HARD)
        )
    )

    @Test
    fun awardsDifficultyBasedXP() {
        val result = completeQuest(initialState(), 2)

        assertTrue(result is QuestResult.Success)

        val success = result as QuestResult.Success
        assertEquals(100, success.state.totalXP)
        assertTrue(success.state.quests[1].completed)
    }

    @Test
    fun preventsDuplicateCompletion() {
        val first = completeQuest(initialState(), 1)
                as QuestResult.Success

        val second = completeQuest(first.state, 1)

        assertEquals(QuestResult.AlreadyCompleted, second)
        assertEquals(50, first.state.totalXP)
    }

    @Test
    fun returnsNotFoundForInvalidId() {
        val result = completeQuest(initialState(), 99)

        assertEquals(QuestResult.NotFound, result)
    }

    @Test
    fun calculatesLevelCorrectly() {
        val state = QuestState(emptyList(), totalXP = 250)

        assertEquals(3, state.level)
    }
}
