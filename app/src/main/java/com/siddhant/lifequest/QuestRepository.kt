package com.siddhant.lifequest

import kotlinx.coroutines.delay

class QuestRepository {
    suspend fun loadQuests(): List<Quest>{
        delay(1500)
        return listOf(
            Quest(
                id=1,
                title="Study DSA",
                xp=20
            ),

            Quest(
                id=2,
                title="Exercise",
                xp=15
            ),

            Quest(
                id=3,
                title="Read a book",
                xp=10
            ),

        )
    }
}