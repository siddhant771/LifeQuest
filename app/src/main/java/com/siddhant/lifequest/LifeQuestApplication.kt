package com.siddhant.lifequest
import android.app.Application
import com.siddhant.lifequest.worker.QuestSyncScheduler
import dagger.hilt.android.HiltAndroidApp
@HiltAndroidApp class LifeQuestApplication:Application(){override fun onCreate(){super.onCreate();QuestSyncScheduler.schedule(this)}}
