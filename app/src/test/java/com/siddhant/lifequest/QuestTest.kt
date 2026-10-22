package com.siddhant.lifequest



import org.junit.Assert.*
import org.junit.Test

class QuestTest {

    private fun initialState() = QuestState(
        listOf(
            Quest(1, "Study Kotlin", 50),
            Quest(2, "Workout", 30)
        )
    )

    @Test
    fun awardsXP() {
        val result = completeQuest(initialState(), 1)

        assertEquals(50, result.totalXP)
        assertTrue(result.quests[0].completed)
    }

    @Test
    fun preventsDuplicateXP() {
        val once = completeQuest(initialState(), 1)
        val twice = completeQuest(once, 1)

        assertEquals(50, twice.totalXP)
    }

    @Test
    fun ignoresUnknownQuest() {
        val original = initialState()
        val result = completeQuest(original, 99)

        assertEquals(original, result)
    }
}
