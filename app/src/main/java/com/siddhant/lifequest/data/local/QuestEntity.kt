package com.siddhant.lifequest.data.local



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class QuestEntity(

    @PrimaryKey
    val id: Int,

    val title: String,

    val xp: Int,

    val difficulty: String,

    val completed: Boolean = false
)