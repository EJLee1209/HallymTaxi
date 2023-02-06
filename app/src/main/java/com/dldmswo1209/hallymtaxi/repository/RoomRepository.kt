package com.dldmswo1209.hallymtaxi.repository

import android.content.Context
import com.dldmswo1209.hallymtaxi.database.AppDatabase
import com.dldmswo1209.hallymtaxi.model.Chat

class RoomRepository(context: Context) {
    private val roomDB = AppDatabase.getInstance(context)

    fun saveChat(chat: Chat) = roomDB?.chatDao()?.saveChat(chat)
    fun detachChatList(roomId: String) = roomDB?.chatDao()?.detachChatList(roomId)
    fun deleteChatHistory(roomId: String) = roomDB?.chatDao()?.deleteChatHistory(roomId)
}