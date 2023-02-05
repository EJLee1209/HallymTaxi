package com.dldmswo1209.hallymtaxi.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dldmswo1209.hallymtaxi.database.AppDatabase
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.retrofit.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class MainRepository(val context: Context) {

    private val client = KakaoApiClient.create()
//    private val fcmClient = FcmServerApiClient.create()
    private val fireStore = Firebase.firestore
    private val welcomeRepository = ServerRepository()

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

        if(room.user1?.uid == user.uid || room.user2?.uid == user.uid || room.user3?.uid == user.uid || room.user4?.uid == user.uid){
            // 이미 방에 속해있음
            result.value = true
        }else{
            if(room.userMaxCount > room.userCount){
                val updateMap = mutableMapOf<String, Any>()
                val receiveTokens : List<String?>
                if(room.user1 == null || room.user2 == null || room.user3 == null || room.user4 == null){
                    when{
                        room.user1 == null ->{
                            updateMap["user1"] = user
                            receiveTokens = listOf( room.user2?.fcmToken, room.user3?.fcmToken, room.user4?.fcmToken)
                        }
                        room.user2 == null ->{
                            updateMap["user2"] = user
                            receiveTokens = listOf( room.user1?.fcmToken, room.user3?.fcmToken, room.user4?.fcmToken)
                        }
                        room.user3 == null ->{
                            updateMap["user3"] = user
                            receiveTokens = listOf( room.user1?.fcmToken, room.user2?.fcmToken, room.user4?.fcmToken)
                        }
                        else ->{
                            updateMap["user4"] = user
                            receiveTokens = listOf( room.user1?.fcmToken, room.user2?.fcmToken, room.user3?.fcmToken)
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
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendMessage(roomId = room.roomId, chat = Chat(roomId = room.roomId, userId = user.uid, msg = "${user.name}님이 입장했습니다" , messageType = CHAT_JOIN), user.name, receiveTokens)
                                        }
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

    suspend fun sendMessage(roomId: String, chat: Chat, senderName: String, receiveTokens: List<String?>){
//        val ref = fireStore.collection("Room").document(roomId).collection("Chat").document()
//        chat.id = ref.id
//        ref.set(chat)

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


    fun updateFcmToken(uid: String, token: String){
        fireStore.collection("User").document(uid).update(mapOf("fcmToken" to token))
    }


}