package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.Chat
import com.dldmswo1209.hallymtaxi.data.model.Place
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

interface DatabaseRepository {
    suspend fun saveChat(chat: Chat)
    suspend fun updateChatById(id: String, sendSuccess: String)
    suspend fun deleteChat(id: String)
    suspend fun detachChatList(roomId: String) : List<Chat>?
    suspend fun insertRoomInfo(roomInfo: RoomInfo)
    suspend fun detachRoomInfo(roomId: String): RoomInfo?
    suspend fun updateRoomInfo(roomInfo: RoomInfo)
    suspend fun detachRoomInfoHistory() : List<RoomInfo>?
    suspend fun getFavorites() : List<Place>
    suspend fun saveFavorite(place: Place)
    suspend fun deleteFavorite(place: Place)

}