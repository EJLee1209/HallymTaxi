package com.dldmswo1209.hallymtaxi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dldmswo1209.hallymtaxi.data.model.Chat

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveChat(vararg chat: Chat)
    @Query("UPDATE Chat SET sendSuccess = :sendSuccess WHERE id = :id")
    fun updateChatById(id: String, sendSuccess: String)
    @Query("DELETE FROM Chat WHERE id = :id")
    fun deleteChat(id: String)
    @Query("SELECT * FROM chat WHERE roomId = :roomId")
    fun detachChatList(roomId: String): List<Chat>
    @Query("DELETE FROM chat WHERE roomId = :roomId")
    fun deleteChatHistory(vararg roomId: String)

}