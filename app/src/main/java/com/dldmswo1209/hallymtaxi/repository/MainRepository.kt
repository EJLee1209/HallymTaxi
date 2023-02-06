package com.dldmswo1209.hallymtaxi.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dldmswo1209.hallymtaxi.database.AppDatabase
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.retrofit.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class MainRepository(val context: Context) {

    private val client = KakaoApiClient.create()
    private val fireStore = Firebase.firestore
    private val welcomeRepository = ServerRepository()

    suspend fun searchKeyword(keyword: String) = client.getSearchKeyword(query = keyword)


    fun createRoom(room: CarPoolRoom, user: User) : LiveData<CarPoolRoom>{
        val newRoom = MutableLiveData<CarPoolRoom>()

        val ref = fireStore.collection("Room").document()
        room.roomId = ref.id
        ref.set(room).addOnSuccessListener {
            newRoom.value = room
            CoroutineScope(Dispatchers.Main).launch {
                sendMessage(roomId = room.roomId, chat = Chat(roomId = room.roomId, userId = user.uid, messageType = CHAT_JOIN), user.name, listOf())
            }
        }

        return newRoom
    }

    fun detachAllRoom() : LiveData<List<CarPoolRoom>>{
        val rooms = MutableLiveData<List<CarPoolRoom>>()

        fireStore.collection("Room").get().addOnSuccessListener {
            if(it == null){
                Log.d("testt", "detachAllRoom: null")
                return@addOnSuccessListener
            }
            val dataList = mutableListOf<CarPoolRoom>()
            it.forEach { data->
                val room = data.toObject<CarPoolRoom>()
                dataList.add(room)
            }
            Log.d("testt", "detachAllRoom()")
            rooms.postValue(dataList)
        }

        return rooms
    }

    fun joinRoom(room: CarPoolRoom, user: User): LiveData<Boolean>{
        val result = MutableLiveData<Boolean>()

        if(room.participants.contains(user)){
            // 이미 방에 속해 있음
            result.postValue(true)
        }else{
            if(room.userMaxCount > room.userCount){
                val receiveTokens = mutableListOf<String?>()

                // transaction 을 사용해서 하나의 document 에 접근 하려는 다수의 작업들을 통제
                // 예를 들어 인원제한이 3명인 채팅방에 현재 2명이 입장해 있고, 2명 이상의 유저가 동시에 채팅방 입장을 하려는 경우
                // 먼저 입장이 완료된 유저만 성공시키고, 나머지 유저는 실패 처리
                val docRef = fireStore.collection("Room").document(room.roomId)
                fireStore.runTransaction {transaction->
                    val snapshot = transaction.get(docRef)
                    val newCount = snapshot.getLong("userCount")?.plus(1) ?: return@runTransaction

                    if(newCount <= room.userMaxCount){
                        transaction.update(docRef, "userCount", FieldValue.increment(1))
                        transaction.update(docRef, "participants", FieldValue.arrayUnion(user))
                    }else{
                        throw FirebaseFirestoreException("userCount limit over", FirebaseFirestoreException.Code.ABORTED)
                    }
                }
                    .addOnSuccessListener {
                        // 입장 성공
                        result.postValue(true)
                        room.participants.add(user)
                        room.participants.forEach { if(it.fcmToken != user.fcmToken) receiveTokens.add(it.fcmToken) }
                        CoroutineScope(Dispatchers.Main).launch {
                            sendMessage(roomId = room.roomId, chat = Chat(roomId = room.roomId, userId = user.uid, msg = "${user.name}님이 입장했습니다" , messageType = CHAT_JOIN), user.name, receiveTokens)
                        }
                    }
                    .addOnFailureListener {
                        // 입장 실패
                        result.postValue(false)
                    }

            }else{
                result.postValue(false)
            }
        }

        return result
    }

    suspend fun sendMessage(roomId: String, chat: Chat, senderName: String, receiveTokens: List<String?>){
        CoroutineScope(Dispatchers.IO).launch {
            RoomRepository(context).saveChat(chat)
        }

        receiveTokens.forEach {
            if(!it.isNullOrEmpty()){
                welcomeRepository.sendPushMessage(it, chat.roomId, chat.userId, senderName, chat.msg, chat.messageType)
            }

        }
        updateRoomInfo(roomId, chat)
    }

    private fun updateRoomInfo(roomId: String, chat: Chat){
//        val roomInfo = RoomInfo(
//            room.roomId,
//            chat.msg,
//            chat.dateTime,
//            chat.id,
//            room.startPlace,
//            room.endPlace
//        )
        val updateRoomInfo = mapOf<String, Any>(
            "msg" to chat.msg,
            "dateTime" to chat.dateTime,
        )


        fireStore.collection("RoomInfo").document(roomId).update(updateRoomInfo)
    }

    // 채팅방 삭제(모든 참여자가 퇴장한 경우)
    private fun deleteRoom(roomId: String){
        fireStore.collection("Room").document(roomId).delete().addOnSuccessListener {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Default) {
                    delay(1000)
                }
                fireStore.collection("RoomInfo").document(roomId).delete()
            }
        }
    }

    // 채팅방 퇴장
    fun exitRoom(user: User, room: CarPoolRoom){
        if(room.userCount == 1){
            // 나 혼자 있으면 바로 채팅방 삭제해버리기
            deleteRoom(room.roomId)
            return
        }

        val updateMap = mapOf<String, Any?>(
            "participants" to FieldValue.arrayRemove(user),
            "userCount" to --room.userCount
        )

        // 채팅방에 나말고도 다른 사람 존재하면, 그냥 나만 퇴장
        fireStore.collection("Room").document(room.roomId).update(updateMap)
    }


    fun updateFcmToken(uid: String, token: String){
        fireStore.collection("User").document(uid).update(mapOf("fcmToken" to token))
    }


}