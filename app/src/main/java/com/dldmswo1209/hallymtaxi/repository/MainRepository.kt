package com.dldmswo1209.hallymtaxi.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.retrofit.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class MainRepository(val context: Context) {

    private val client = KakaoApiClient.create()
    private val fireStore = Firebase.firestore
    private val serverRepository = ServerRepository()

    suspend fun searchKeyword(keyword: String) = client.getSearchKeyword(query = keyword)


    fun createRoom(room: CarPoolRoom, user: User) : LiveData<CarPoolRoom>{
        val newRoom = MutableLiveData<CarPoolRoom>()

        val ref = fireStore.collection("Room").document()
        room.roomId = ref.id
        ref.set(room).addOnSuccessListener {
            newRoom.value = room
            CoroutineScope(Dispatchers.Main).launch {
                sendMessage(chat = Chat(roomId = room.roomId, userId = user.uid, userName = user.name, messageType = CHAT_JOIN), user.name, listOf())
            }
        }

        return newRoom
    }

    fun joinRoom(room: CarPoolRoom, user: User): LiveData<Boolean>{
        val result = MutableLiveData<Boolean>()

        if(room.participants.contains(user)){
            // 이미 방에 속해 있음
            result.postValue(true)
        }else{
            if(room.userMaxCount > room.userCount){
                val receiveTokens = mutableListOf<String?>()

                val docRef = fireStore.collection("Room").document(room.roomId)
                docRef.get().addOnSuccessListener {
                    if(it.data == null){
                        result.postValue(false)
                        return@addOnSuccessListener
                    }else{
                        // transaction 을 사용해서 하나의 document 에 접근 하려는 다수의 작업들을 통제
                        // 예를 들어 인원제한이 3명인 채팅방에 현재 2명이 입장해 있고, 2명 이상의 유저가 동시에 채팅방 입장을 하려는 경우
                        // 먼저 입장이 완료된 유저만 성공시키고, 나머지 유저는 실패 처리
                        fireStore.runTransaction {transaction->
                            val snapshot = transaction.get(docRef)
                            val newCount = snapshot.getLong("userCount")?.plus(1) ?: return@runTransaction
                            val closed = snapshot.getBoolean("closed") ?: return@runTransaction

                            if(newCount <= room.userMaxCount && !closed){
                                transaction.update(docRef, "userCount", FieldValue.increment(1))
                                transaction.update(docRef, "participants", FieldValue.arrayUnion(user))
                            }else{
                                throw FirebaseFirestoreException("userCount limit over or closed room", FirebaseFirestoreException.Code.ABORTED)
                            }
                        }
                            .addOnSuccessListener {
                                // 입장 성공
                                result.postValue(true)
                                room.participants.add(user)
                                room.participants.forEach { if(it.fcmToken != user.fcmToken) receiveTokens.add(it.fcmToken) }
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendMessage(chat = Chat(roomId = room.roomId, userId = user.uid, userName = user.name ,msg = "${user.name}님이 입장하셨습니다" , messageType = CHAT_JOIN), user.name, receiveTokens)
                                }
                            }
                            .addOnFailureListener {
                                // 입장 실패
                                result.postValue(false)
                            }
                    }

                }

            }else{
                result.postValue(false)
            }
        }

        return result
    }

    suspend fun sendMessage(chat: Chat, senderName: String, receiveTokens: List<String?>){
        CoroutineScope(Dispatchers.IO).launch {
            RoomRepository(context).saveChat(chat)
            if(chat.messageType != CHAT_EXIT) {
                RoomRepository(context).insertRoomInfo(RoomInfo(chat.roomId, chat.msg, chat.dateTime, false, isActivate = true))
            }
        }

        receiveTokens.forEach {
            if(!it.isNullOrEmpty()){
                serverRepository.sendPushMessage(it, chat.roomId, chat.userId, senderName, chat.msg, chat.messageType)
            }
        }
    }

    // 채팅방 삭제(모든 참여자가 퇴장한 경우)
    private fun deleteRoom(roomId: String){
        fireStore.collection("Room").document(roomId).delete().addOnSuccessListener {
            fireStore.collection("RoomInfo").document(roomId).delete()
        }
    }

    // 채팅방 퇴장
    fun exitRoom(user: User, room: CarPoolRoom){
        val docRef = fireStore.collection("Room").document(room.roomId)
        fireStore.runTransaction{transaction->
            val snapshot = transaction.get(docRef)
            val userCount = snapshot.getLong("userCount")?.minus(1) ?: return@runTransaction

            if(userCount > 0){
                transaction.update(docRef,"userCount",userCount)
                transaction.update(docRef, "participants", FieldValue.arrayRemove(user))
            }else{
                deleteRoom(room.roomId) // 채팅방에 유저수가 0이므로 채팅방 삭제
            }
        }
    }

    fun deactivateRoom(roomId: String) {
        fireStore.collection("Room").document(roomId).update("closed", true)
    }

    fun updateFcmToken(uid: String, token: String){
        fireStore.collection("User").document(uid).update(mapOf("fcmToken" to token))
    }


}