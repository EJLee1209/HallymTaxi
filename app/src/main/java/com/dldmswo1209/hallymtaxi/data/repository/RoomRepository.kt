package com.dldmswo1209.hallymtaxi.data.repository

import android.content.Context
import com.dldmswo1209.hallymtaxi.data.database.AppDatabase
import com.dldmswo1209.hallymtaxi.data.model.Chat
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

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