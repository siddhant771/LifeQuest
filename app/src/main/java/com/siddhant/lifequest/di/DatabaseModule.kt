package com.siddhant.lifequest.di
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.siddhant.lifequest.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class) object DatabaseModule{
 private val M1=object:Migration(1,2){override fun migrate(db:SupportSQLiteDatabase){db.execSQL("ALTER TABLE quests ADD COLUMN remoteId INTEGER");db.execSQL("ALTER TABLE quests ADD COLUMN pendingCreate INTEGER NOT NULL DEFAULT 1");db.execSQL("ALTER TABLE quests ADD COLUMN pendingCompletion INTEGER NOT NULL DEFAULT 0")}}
 private val M2=object:Migration(2,3){override fun migrate(db:SupportSQLiteDatabase){db.execSQL("ALTER TABLE quests ADD COLUMN clientRequestId TEXT")}}
 @Provides @Singleton fun provideQuestDatabase(@ApplicationContext context:Context):QuestDatabase=Room.databaseBuilder(context,QuestDatabase::class.java,"lifequest_database").addMigrations(M1,M2).build()
 @Provides fun provideQuestDao(db:QuestDatabase):QuestDao=db.questDao()
}
