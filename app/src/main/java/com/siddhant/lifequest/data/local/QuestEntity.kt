package com.siddhant.lifequest.data.local
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="quests")
data class QuestEntity(
 @PrimaryKey val id:Int,val title:String,val xp:Int,val difficulty:String,val completed:Boolean=false,
 val remoteId:Long?=null,@ColumnInfo(defaultValue="1") val pendingCreate:Boolean=true,
 @ColumnInfo(defaultValue="0") val pendingCompletion:Boolean=false,val clientRequestId:String?=null
)
