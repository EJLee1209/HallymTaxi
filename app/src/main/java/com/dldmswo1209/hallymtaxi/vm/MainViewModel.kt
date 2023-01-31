package com.dldmswo1209.hallymtaxi.vm

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dldmswo1209.hallymtaxi.SplashActivity
import com.dldmswo1209.hallymtaxi.common.context
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.repository.MainRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import java.sql.Timestamp

class MainViewModel(
    private val repository: MainRepository,
    application: Application
) : AndroidViewModel(application) {

    private val sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE)
    private val uid = sharedPreferences.getString("uid", "").toString()

    private val fireStore = Firebase.firestore
    private val auth = Firebase.auth
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

    fun logout(activity: Activity) {
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
            if (isStartPoint) _startPoint.postValue(repository.searchKeyword(keyword))
            else _endPoint.postValue(repository.searchKeyword(keyword))
        } catch (e: Exception) {
            networkErrorMessage(e)
        }
    }

    fun getUserInfo(): LiveData<User>? {
        val user = MutableLiveData<User>()

        if (uid.isBlank()) return null

        repository.getUserInfo(uid).observeForever {
            user.value = it
        }

        return user
    }

    fun createRoom(room: CarPoolRoom, user: User) {
        repository.createRoom(room, user).observeForever {
            sharedPreferences.edit().putString("joinedRoom", room.roomId).apply()
            _isCreated.value = it
        }
    }

    fun detachAllRoom() {
        repository.detachAllRoom().observeForever {
            _poolList.postValue(it)
        }
    }

    fun joinRoom(room: CarPoolRoom, user: User) {
        repository.joinRoom(room, user).observeForever {
            if (it) {
                sharedPreferences.edit().putString("joinedRoom", room.roomId).apply()
            }
            _isJoined.value = it
        }
    }

    fun sendMessage(room: CarPoolRoom, chat: Chat) {
        repository.sendMessage(room, chat)
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

    fun getMessage(roomId: String): LiveData<List<Chat>> {
        val chatList = MutableLiveData<List<Chat>>()

        messageListener = fireStore.collection("Room").document(roomId).collection("Chat")
            .addSnapshotListener { query, error ->
                if (error != null) {
                    Log.d("testt", "getMessage: ${error}")
                    return@addSnapshotListener
                }

                query?.let {
                    val dataList = mutableListOf<Chat>()
                    it.forEach { data ->
                        val chat = data.toObject<Chat>()
                        dataList.add(chat)
                    }
                    if (dataList.size == 0) return@addSnapshotListener
                    chatList.postValue(dataList)
                }

                Log.d("testt", "메세지 가져오기!!")
            }

        return chatList
    }

    fun getHistoryMessage(roomId: String) : LiveData<List<Chat>> {
        val chatList = MutableLiveData<List<Chat>>()

        historyMessageListener = fireStore.collection("History").document(uid).collection("user_history").document(roomId)
            .collection("Chat").addSnapshotListener { query, error ->
            if (error != null) {
                Log.d("testt", "getHistoryMessage: ${error}")
                return@addSnapshotListener
            }

            query?.let {
                val dataList = mutableListOf<Chat>()
                it.forEach { data ->
                    val chat = data.toObject<Chat>()
                    dataList.add(chat)
                }
                if (dataList.size == 0) return@addSnapshotListener
                chatList.postValue(dataList)
            }

            Log.d("testt", "이전 메세지 가져오기!!")
        }

        return chatList
    }

    fun exitRoom(user: String, room: CarPoolRoom) {
        repository.exitRoom(user, room)
        sharedPreferences.edit().putString("joinedRoom", "").apply()
    }

    fun saveHistory(room: CarPoolRoom, messageList: List<Chat>) {
        repository.saveHistory(uid, room, messageList)
    }

    fun detachHistory(): LiveData<List<RoomInfo>> {
        val historyList = MutableLiveData<List<RoomInfo>>()

        repository.detachHistory(uid).observeForever {
            historyList.postValue(it)
        }

        return historyList
    }
    fun allListenerRemove() {
        messageListener?.remove()
        roomListener?.remove()
        historyMessageListener?.remove()
        roomInfoListener?.remove()
    }

    fun getFcmToken(): LiveData<String> {
        val token = MutableLiveData<String>()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("testt", "getFcmToken: failed", task.exception)
                return@addOnCompleteListener
            }

            token.value = task.result
        }

        return token
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