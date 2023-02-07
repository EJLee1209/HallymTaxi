package com.dldmswo1209.hallymtaxi.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dldmswo1209.hallymtaxi.model.Chat
import com.dldmswo1209.hallymtaxi.model.RoomInfo

@Database(entities = [Chat::class, RoomInfo::class], version = 3)
abstract class AppDatabase : RoomDatabase(){
    abstract fun chatDao(): ChatDao
    abstract fun roomInfoDao(): RoomInfoDao

    // 데이터 베이스 객체를 싱글톤으로 인스턴스.
    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if (instance == null)
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "hallymTaxi.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            return instance
        }

        fun destroyInstance() {
            instance = null
        }
    }
}