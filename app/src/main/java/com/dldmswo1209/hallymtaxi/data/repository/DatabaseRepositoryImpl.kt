package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.database.ChatDao
import com.dldmswo1209.hallymtaxi.data.database.RoomInfoDao
import com.dldmswo1209.hallymtaxi.data.model.Chat
import com.dldmswo1209.hallymtaxi.data.model.RoomInfo

class DatabaseRepositoryImpl(
    private val chatDao: ChatDao,
    private val roomInfoDao: RoomInfoDao
) : DatabaseRepository {
    override fun saveChat(chat: Chat) { chatDao.saveChat(chat) }
    override fun updateChatById(id: String, sendSuccess: String) { chatDao.updateChatById(id, sendSuccess) }
    override fun deleteChat(id: String) { chatDao.deleteChat(id) }
    override fun detachChatList(roomId: String) = chatDao.detachChatList(roomId)
    override fun insertRoomInfo(roomInfo: RoomInfo) { roomInfoDao.insertRoomInfo(roomInfo) }
    override fun updateRoomInfo(roomInfo: RoomInfo) { roomInfoDao.updateRoomInfo(roomInfo) }
    override fun detachRoomInfo(roomId: String): RoomInfo = roomInfoDao.detachRoomInfo(roomId)
    override fun detachRoomInfoHistory(): List<RoomInfo> = roomInfoDao.detachRoomInfoHistory()
}