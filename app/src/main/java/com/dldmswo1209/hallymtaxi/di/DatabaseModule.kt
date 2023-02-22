package com.dldmswo1209.hallymtaxi.di

import android.content.Context
import androidx.room.Room
import com.dldmswo1209.hallymtaxi.data.local.AppDatabase
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
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "hallymTaxi.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideChatDao(
        appDatabase: AppDatabase
    ) = appDatabase.chatDao()

    @Provides
    @Singleton
    fun provideRoomInfoDao(
        appDatabase: AppDatabase
    ) = appDatabase.roomInfoDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(
        appDatabase: AppDatabase
    ) = appDatabase.favoriteDao()

}