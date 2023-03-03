package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.data.UiState
import kotlinx.coroutines.flow.Flow

interface FireStoreRepository {
    fun subscribeUser() : Flow<User>
    fun subscribeMyRoom(): Flow<CarPoolRoom>
    fun subscribeParticipantsTokens(roomId: String): Flow<List<String>>
    fun findUserName(uid: String, result: (UiState<String>) -> Unit)
    fun getParticipantsTokens(roomId: String, result: (UiState<List<String>>) -> Unit)
    fun createRoom(room: CarPoolRoom, result: (UiState<CarPoolRoom>) -> Unit)
    fun joinRoom(room: CarPoolRoom, result: (UiState<String>) -> Unit)
    fun exitRoom(room: CarPoolRoom, result: (UiState<String>)->Unit)
    fun deactivateRoom(roomId: String, result: (UiState<String>)->Unit)
    fun updateFcmToken(result: (UiState<String>)->Unit)
    fun monitoringLoggedIn() : Flow<SignedIn>
}