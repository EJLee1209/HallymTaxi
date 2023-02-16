package com.dldmswo1209.hallymtaxi.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.util.UiState
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface FireStoreRepository {

    fun createRoom(room: CarPoolRoom, user: User, result: (UiState<CarPoolRoom>) -> Unit)
    fun joinRoom(room: CarPoolRoom, user: User, result: (UiState<String>) -> Unit)
    fun deleteRoom(roomId: String)
    fun exitRoom(user: User, room: CarPoolRoom, result: (UiState<String>)->Unit)
    fun deactivateRoom(roomId: String, result: (UiState<String>)->Unit)
    fun updateFcmToken(uid: String, token: String)

}