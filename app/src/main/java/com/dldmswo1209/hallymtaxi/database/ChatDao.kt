package com.dldmswo1209.hallymtaxi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dldmswo1209.hallymtaxi.model.Chat

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveChat(vararg chat: Chat)

    @Query("SELECT * FROM chat WHERE roomId = :roomId")
    fun detachChatList(roomId: String): List<Chat>

    @Query("DELETE FROM chat WHERE roomId = :roomId")
    fun deleteChatHistory(vararg roomId: String)

}