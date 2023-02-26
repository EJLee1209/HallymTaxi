package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.data.UiState
import kotlinx.coroutines.flow.Flow

interface FireStoreRepository {
    fun subscribeUser() : Flow<User>
    fun subscribeMyRoom(user: User): Flow<CarPoolRoom>
    fun getMyRoom(user: User, result: (UiState<CarPoolRoom>) -> Unit)
    fun createRoom(room: CarPoolRoom, user: User, result: (UiState<CarPoolRoom>) -> Unit)
    fun joinRoom(room: CarPoolRoom, user: User, result: (UiState<String>) -> Unit)
    fun exitRoom(user: User, room: CarPoolRoom, result: (UiState<String>)->Unit)
    fun deactivateRoom(roomId: String, result: (UiState<String>)->Unit)
    fun updateFcmToken(result: (UiState<String>)->Unit)
    fun updateRoomParticipantsInfo(roomId: String, participants: List<User>, currentUser: User)
}