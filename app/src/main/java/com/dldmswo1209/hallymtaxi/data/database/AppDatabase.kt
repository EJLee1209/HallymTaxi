package com.dldmswo1209.hallymtaxi.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dldmswo1209.hallymtaxi.data.database.dao.ChatDao
import com.dldmswo1209.hallymtaxi.data.database.dao.FavoriteDao
import com.dldmswo1209.hallymtaxi.data.database.dao.RoomInfoDao
import com.dldmswo1209.hallymtaxi.data.model.Chat
import com.dldmswo1209.hallymtaxi.data.model.Place
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

@Database(entities = [Chat::class, RoomInfo::class, Place::class], version = 10)
abstract class AppDatabase : RoomDatabase(){
    abstract fun chatDao(): ChatDao
    abstract fun roomInfoDao(): RoomInfoDao
    abstract fun favoriteDao(): FavoriteDao
}