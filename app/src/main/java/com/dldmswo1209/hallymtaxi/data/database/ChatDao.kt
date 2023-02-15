package com.dldmswo1209.hallymtaxi.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dldmswo1209.hallymtaxi.data.model.Chat

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveChat(vararg chat: Chat)

    @Update
    fun updateChat(vararg chat: Chat)

    @Query("SELECT * FROM chat WHERE roomId = :roomId")
    fun detachChatList(roomId: String): List<Chat>

    @Query("DELETE FROM chat WHERE roomId = :roomId")
    fun deleteChatHistory(vararg roomId: String)

}