package com.dldmswo1209.hallymtaxi.repository

import android.content.Context
import com.dldmswo1209.hallymtaxi.database.AppDatabase
import com.dldmswo1209.hallymtaxi.model.Chat
import com.dldmswo1209.hallymtaxi.model.RoomInfo

class RoomRepository(context: Context) {
    private val roomDB = AppDatabase.getInstance(context)
    fun saveChat(chat: Chat) = roomDB?.chatDao()?.saveChat(chat)
    fun detachChatList(roomId: String) = roomDB?.chatDao()?.detachChatList(roomId)
//    fun deleteChatHistory(roomId: String) = roomDB?.chatDao()?.deleteChatHistory(roomId)
    fun insertRoomInfo(roomInfo: RoomInfo) = roomDB?.roomInfoDao()?.insertRoomInfo(roomInfo)
    fun detachRoomInfo(roomId: String) = roomDB?.roomInfoDao()?.detachRoomInfo(roomId)
    fun updateRoomInfo(roomInfo: RoomInfo) = roomDB?.roomInfoDao()?.updateRoomInfo(roomInfo)
    fun detachRoomInfoHistory() = roomDB?.roomInfoDao()?.detachRoomInfoHistory()
}