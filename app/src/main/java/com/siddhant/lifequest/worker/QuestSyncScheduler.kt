package com.siddhant.lifequest.worker
import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
object QuestSyncScheduler{
 fun schedule(context:Context){
  val request=PeriodicWorkRequestBuilder<QuestSyncWorker>(15,TimeUnit.MINUTES)
   .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
  WorkManager.getInstance(context).enqueueUniquePeriodicWork("lifequest-quest-sync",ExistingPeriodicWorkPolicy.KEEP,request)
 }
}
