package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.Chat
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

interface DatabaseRepository {

    fun saveChat(chat: Chat)
    fun updateChat(chat: Chat)
    fun detachChatList(roomId: String) : List<Chat>?
    fun insertRoomInfo(roomInfo: RoomInfo)
    fun detachRoomInfo(roomId: String): RoomInfo?
    fun updateRoomInfo(roomInfo: RoomInfo)
    fun detachRoomInfoHistory() : List<RoomInfo>?

}