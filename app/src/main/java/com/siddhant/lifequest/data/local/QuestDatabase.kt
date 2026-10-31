package com.siddhant.lifequest.data.local



import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QuestEntity::class],
    version = 1,
    exportSchema = true
)
abstract class QuestDatabase : RoomDatabase() {

    abstract fun questDao(): QuestDao
}