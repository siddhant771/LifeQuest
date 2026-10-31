package com.siddhant.lifequest.di


import android.content.Context
import androidx.room.Room
import com.siddhant.lifequest.data.local.QuestDao
import com.siddhant.lifequest.data.local.QuestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuestDatabase(
        @ApplicationContext context: Context
    ): QuestDatabase {

        return Room.databaseBuilder(
            context,
            QuestDatabase::class.java,
            "lifequest_database"
        ).build()
    }

    @Provides
    fun provideQuestDao(
        database: QuestDatabase
    ): QuestDao {

        return database.questDao()
    }
}