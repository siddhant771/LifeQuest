package com.siddhant.lifequest.data.repository

import com.siddhant.lifequest.domain.model.Quest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestRepositoryTest {

    @Test
    fun getQuests_returnsInitialQuests() = runTest {
        val repository = QuestRepositoryImp()

        val quests = repository.getQuests().first()

        assertEquals(3, quests.size)
        assertEquals("Study Kotlin", quests[0].title)
    }

    @Test
    fun addQuest_addsNewQuest() = runTest {
        val repository = QuestRepositoryImp()

        repository.addQuest(
            Quest(4, "Learn Hilt", 40)
        )

        val quests = repository.getQuests().first()

        assertEquals(4, quests.size)
        assertEquals("Learn Hilt", quests.last().title)
    }

    @Test
    fun completeQuest_marksQuestAsCompleted() = runTest {
        val repository = QuestRepositoryImp()

        repository.completeQuest(1)

        val quests = repository.getQuests().first()

        assertTrue(quests.first { it.id == 1 }.completed)
    }
}