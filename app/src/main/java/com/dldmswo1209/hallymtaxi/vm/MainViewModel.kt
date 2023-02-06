package com.dldmswo1209.hallymtaxi.vm

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.common.context
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.repository.MainRepository
import com.dldmswo1209.hallymtaxi.repository.RoomRepository
import com.dldmswo1209.hallymtaxi.repository.ServerRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*

class MainViewModel(
    private val mainRepository: MainRepository,
    private val roomRepository: RoomRepository,
    private val serverRepository: ServerRepository,
    application: Application
) : AndroidViewModel(application) {

    private val sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE)
    private val uid = sharedPreferences.getString("uid", "").toString()

    private val fireStore = Firebase.firestore
    private val auth = Firebase.auth

    private var userListener: ListenerRegistration? = null
    private var messageListener: ListenerRegistration? = null
    private var historyMessageListener: ListenerRegistration? = null
    private var roomListener: ListenerRegistration? = null
    private var roomInfoListener: ListenerRegistration? = null

    private var _startPoint = MutableLiveData<ResultSearchKeyword>()
    val startPoint: LiveData<ResultSearchKeyword> = _startPoint

    private var _endPoint = MutableLiveData<ResultSearchKeyword>()
    val endPoint: LiveData<ResultSearchKeyword> = _endPoint

    private var _poolList = MutableLiveData<List<CarPoolRoom>>()
    val poolList: LiveData<List<CarPoolRoom>> = _poolList

    private var _isCreated = MutableLiveData<CarPoolRoom>()
    val isCreated: LiveData<CarPoolRoom> = _isCreated

    private var _isJoined = MutableLiveData<Boolean>()
    val isJoined: LiveData<Boolean> = _isJoined

    private var _chatList = MutableLiveData<List<Chat>>()
    val chatList : LiveData<List<Chat>> = _chatList

    fun logout(activity: Activity, uid: String) {
        val userInfo = mapOf<String, Any>(
            "fcmToken" to ""
        )
        fireStore.collection("User").document(uid).update(userInfo)

        sharedPreferences.edit().run {
            putString("uid", "")
            putString("joinedRoom", "")
        }.apply()

        auth.signOut()
        activity.finish()
        activity.startActivity(Intent(activity, SplashActivity::class.java))
    }

    fun searchKeyword(keyword: String, isStartPoint: Boolean) = viewModelScope.launch {
        try {
            if (isStartPoint) _startPoint.postValue(mainRepository.searchKeyword(keyword))
            else _endPoint.postValue(mainRepository.searchKeyword(keyword))
        } catch (e: Exception) {
            networkErrorMessage(e)
        }
    }

    fun getUserInfo(): LiveData<User>? {
        val user = MutableLiveData<User>()

        if (uid.isEmpty()) return null
        getFcmToken()
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

    fun subscribeUser() : LiveData<User>? {
        val user = MutableLiveData<User>()

        if (uid.isEmpty()) return null
        getFcmToken()
        userListener = fireStore.collection("User").document(uid).addSnapshotListener { value, error->
            if(error != null){
                Log.d("testt", "getUserInfo: fail to get user info")
                return@addSnapshotListener
            }
            value?.let {snapshot->
                user.value = snapshot.toObject<User>()
                Log.d("testt", "subscribeUser()")
            }
        }
        return user
    }

    fun createRoom(room: CarPoolRoom, user: User) {
        mainRepository.createRoom(room, user).observeForever {
            sharedPreferences.edit().putString("joinedRoom", room.roomId).apply()
            _isCreated.value = it
        }
    }

    fun detachAllRoom() {
        mainRepository.detachAllRoom().observeForever {
            _poolList.postValue(it)
        }
    }

    fun joinRoom(room: CarPoolRoom, user: User) {
        mainRepository.joinRoom(room, user).observeForever {
            if (it) {
                sharedPreferences.edit().putString("joinedRoom", room.roomId).apply()
            }
            _isJoined.value = it
        }
    }

    suspend fun sendMessage(chat: Chat, userName: String, receiveTokens: List<String?>) {
        receiveTokens.forEach {
            if(!it.isNullOrEmpty()){
                serverRepository.sendPushMessage(token = it, roomId = chat.roomId, userId = chat.userId, userName = userName, message = chat.msg, messageType = chat.messageType, id = "")
            }
        }
        roomRepository.saveChat(chat)
    }

    fun detachRoom(roomId: String): LiveData<CarPoolRoom> { // 현재 내가 참여 중인 채팅방을 실시간으로 가져옴
        val room = MutableLiveData<CarPoolRoom>()

        roomListener =
            fireStore.collection("Room").document(roomId).addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("testt", "detachRoom: ${error}")
                    return@addSnapshotListener
                }
                value?.let { snapshot ->
                    room.value = snapshot.toObject<CarPoolRoom>()
                }

                Log.d("testt", "방 정보 가져오기!")
            }



        return room
    }

    fun getMyRoom(user: User) : LiveData<CarPoolRoom>{
        val room = MutableLiveData<CarPoolRoom>()

        fireStore.collection("Room")
            .whereEqualTo("user2",user)

            .addSnapshotListener { value, error ->
                if(error != null){
                    Log.d("testt", "getMyRoom: ${error}")
                    return@addSnapshotListener
                }
                value?.let {querySnapshot ->
                    if(!querySnapshot.isEmpty) {
                        val snapshot = querySnapshot.first()
                        room.postValue(snapshot.toObject())
                    }
                }
            }

        return room
    }
    fun detachRoomInfo(roomId: String) : LiveData<RoomInfo>{
        val roomInfo = MutableLiveData<RoomInfo>()

        roomInfoListener = fireStore.collection("RoomInfo").document(roomId).addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("testt", "detachRoomInfo: ${error}")
                return@addSnapshotListener
            }
            value?.let { snapshot ->
                roomInfo.value = snapshot.toObject<RoomInfo>()
            }

            Log.d("testt", "detachRoomInfo")
        }
        return roomInfo
    }

    fun exitRoom(user: String, room: CarPoolRoom) {
        mainRepository.exitRoom(user, room)
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.deleteChatHistory(room.roomId)
        }
        sharedPreferences.edit().putString("joinedRoom", "").apply()
    }

    fun allListenerRemove() {
        messageListener?.remove()
        roomListener?.remove()
        historyMessageListener?.remove()
        roomInfoListener?.remove()
    }

    fun userListenerRemove(){
        userListener?.remove()
    }

    fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("testt", "getFcmToken: failed", task.exception)
                return@addOnCompleteListener
            }

            updateFcmToken(token = task.result)
        }
    }

    private fun updateFcmToken(token: String){
        mainRepository.updateFcmToken(uid, token)
    }

    fun detachChatList(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
        _chatList.postValue(roomRepository.detachChatList(roomId))
    }

    private fun networkErrorMessage(e: Exception){
        Toast.makeText(this@MainViewModel.context, "네트워크 연결상태를 확인해주세요", Toast.LENGTH_SHORT)
            .show()
        e.printStackTrace()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("testt", "onCleared: ")
        allListenerRemove()
    }

}