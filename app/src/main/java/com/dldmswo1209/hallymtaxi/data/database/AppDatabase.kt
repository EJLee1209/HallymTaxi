package com.dldmswo1209.hallymtaxi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dldmswo1209.hallymtaxi.data.model.Chat
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

@Database(entities = [Chat::class, RoomInfo::class], version = 6)
abstract class AppDatabase : RoomDatabase(){
    abstract fun chatDao(): ChatDao
    abstract fun roomInfoDao(): RoomInfoDao
}