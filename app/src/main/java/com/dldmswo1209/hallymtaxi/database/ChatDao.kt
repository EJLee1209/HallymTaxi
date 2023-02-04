package com.dldmswo1209.hallymtaxi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dldmswo1209.hallymtaxi.model.Chat

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveChat(chat: Chat)

    @Query("SELECT * FROM chat WHERE roomId = :roomId")
    fun detachChatList(roomId: String): List<Chat>

//    @Query("SELECT * FROM text_table")
//    fun getAllData() : List<TextEntity>
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    fun insert(text: TextEntity)
//
//    @Query("DELETE FROM text_table")
//    fun deleteAllData()
}