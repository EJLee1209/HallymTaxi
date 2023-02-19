package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.Chat
import com.dldmswo1209.hallymtaxi.data.model.Place
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

interface DatabaseRepository {
    fun saveChat(chat: Chat)
    fun updateChatById(id: String, sendSuccess: String)
    fun deleteChat(id: String)
    fun detachChatList(roomId: String) : List<Chat>?
    fun insertRoomInfo(roomInfo: RoomInfo)
    fun detachRoomInfo(roomId: String): RoomInfo?
    fun updateRoomInfo(roomInfo: RoomInfo)
    fun detachRoomInfoHistory() : List<RoomInfo>?
    fun getFavorites() : List<Place>
    suspend fun saveFavorite(place: Place)
    suspend fun deleteFavorite(place: Place)

}