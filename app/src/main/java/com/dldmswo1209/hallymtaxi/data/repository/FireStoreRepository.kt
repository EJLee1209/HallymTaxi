package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.data.UiState

interface FireStoreRepository {
    fun createRoom(room: CarPoolRoom, user: User, result: (UiState<CarPoolRoom>) -> Unit)
    fun joinRoom(room: CarPoolRoom, user: User, result: (UiState<String>) -> Unit)
    fun exitRoom(user: User, room: CarPoolRoom, result: (UiState<String>)->Unit)
    fun deactivateRoom(roomId: String, result: (UiState<String>)->Unit)
    fun updateFcmToken(result: (UiState<String>)->Unit)
}