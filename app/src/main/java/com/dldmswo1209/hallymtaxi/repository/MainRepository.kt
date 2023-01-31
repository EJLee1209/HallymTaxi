package com.dldmswo1209.hallymtaxi.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.model.Chat
import com.dldmswo1209.hallymtaxi.model.RoomInfo
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.retrofit.KakaoApiClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.sql.Timestamp
import java.time.LocalDateTime

class MainRepository {

    private val client = KakaoApiClient.create()
    private val fireStore = Firebase.firestore

    suspend fun searchKeyword(keyword: String) = client.getSearchKeyword(query = keyword)

    fun getUserInfo(uid: String) : LiveData<User>{
        val user = MutableLiveData<User>()

        fireStore.collection("User").document(uid).get().addOnSuccessListener {
            if(it == null){
                Log.d("testt", "getUserInfo: fail to get user info")
                return@addOnSuccessListener
            }
            user.value = it.toObject<User>()
            Log.d("testt", "getUserInfo()")
        }

        return user
    }

    fun createRoom(room: CarPoolRoom, user: User) : LiveData<CarPoolRoom>{
        val newRoom = MutableLiveData<CarPoolRoom>()

        val ref = fireStore.collection("Room").document()
        room.roomId = ref.id
        ref.set(room).addOnSuccessListener {
            newRoom.value = room
            sendMessage(room = room, chat = Chat(userInfo = user, joinMsg = true, dateTime = LocalDateTime.now().toString()))
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

        if(room.user1?.uid == user.uid || room.user2?.uid == user.uid || room.user3?.uid == user.uid || room.user4?.uid == user.uid){
            // 이미 방에 속해있음
            result.value = true
        }else{
            if(room.userMaxCount > room.userCount){
                val updateMap = mutableMapOf<String, Any>()
                if(room.user1 == null || room.user2 == null || room.user3 == null || room.user4 == null){
                    when{
                        room.user1 == null ->{
                            updateMap["user1"] = user
                        }
                        room.user2 == null ->{
                            updateMap["user2"] = user
                        }
                        room.user3 == null ->{
                            updateMap["user3"] = user
                        }
                        else ->{
                            updateMap["user4"] = user
                        }
                    }
                    updateMap["userCount"] = room.userCount + 1

                    fireStore.collection("Room").document(room.roomId).update(updateMap).addOnSuccessListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            async {
                                delay(500)
                            }.await()
                            fireStore.collection("Room").document(room.roomId).get().addOnCompleteListener {
                                if(it.isSuccessful){
                                    val fetchedRoom = it.result.toObject<CarPoolRoom>() ?: return@addOnCompleteListener
                                    if(fetchedRoom.user1?.uid == user.uid || fetchedRoom.user2?.uid == user.uid || fetchedRoom.user3?.uid == user.uid || fetchedRoom.user4?.uid == user.uid){
                                        result.postValue(true)
                                        sendMessage(room = room, chat = Chat(userInfo = user, msg = "${user.name}님이 입장했습니다" ,joinMsg = true, dateTime = LocalDateTime.now().toString()))
                                    }

                                    else result.postValue(false)
                                }
                            }
                        }
                    }
                        .addOnFailureListener {
                            result.postValue(false)
                        }
                }else{
                    result.postValue(false)
                }
            }else{
                result.postValue(false)
            }
        }

        return result
    }

    fun sendMessage(room: CarPoolRoom, chat: Chat){
        val timeStamp = Timestamp(System.currentTimeMillis())
        val chatKey = java.lang.String.valueOf(timeStamp.time)

        val ref = fireStore.collection("Room").document(room.roomId).collection("Chat").document(chatKey)
        chat.chat_key = chatKey
        ref.set(chat)

        updateRoomInfo(room, chat)
    }

    private fun updateRoomInfo(room: CarPoolRoom, chat: Chat){
        val roomInfo = RoomInfo(
            room.roomId,
            chat.msg,
            chat.dateTime,
            chat.chat_key,
            room.startPlace,
            room.endPlace
        )

        fireStore.collection("RoomInfo").document(room.roomId).set(roomInfo)
    }

    // 채팅방 삭제(모든 참여자가 퇴장한 경우)
    fun deleteRoom(roomId: String){
        fireStore.collection("Room").document(roomId).delete()
    }

    // 채팅방 퇴장
    fun exitRoom(user: String, room: CarPoolRoom){
        if(room.userCount == 1){
            // 나 혼자 있으면 바로 채팅방 삭제해버리기
            deleteRoom(room.roomId)
            return
        }

        val updateMap = mapOf<String, Any?>(
            user to null,
            "userCount" to --room.userCount
        )

        // 채팅방에 나말고도 다른 사람 존재하면, 그냥 나만 퇴장
        fireStore.collection("Room").document(room.roomId).update(updateMap)
    }

    fun saveHistory(uid: String, room: CarPoolRoom, messageList: List<Chat>){
        val ref = fireStore.collection("History").document(uid).collection("user_history").document(room.roomId)
        ref.set(room).addOnSuccessListener {
            messageList.forEach {chat->
                ref.collection("Chat").document(chat.chat_key).set(chat)
            }
        }
    }

    fun detachHistory(uid: String) : LiveData<List<RoomInfo>>{
        val historyList = MutableLiveData<List<RoomInfo>>()
        fireStore.collection("History").document(uid).collection("user_history").get().addOnSuccessListener {
            if(it == null){
                Log.d("testt", "detachHistory: null")
                return@addOnSuccessListener
            }
            val dataList = mutableListOf<RoomInfo>()
            it.forEach { data->
                val room = data.toObject<RoomInfo>()
                dataList.add(room)
            }
            Log.d("testt", "detachHistory()")
            historyList.postValue(dataList)
        }

        return historyList
    }
}