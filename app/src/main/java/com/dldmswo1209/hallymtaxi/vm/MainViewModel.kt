package com.dldmswo1209.hallymtaxi.vm

import android.app.Activity
import android.app.Application
import android.app.DirectAction
import android.content.Context
import android.content.Intent
import android.graphics.Path.Direction
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.room.Room
import com.dldmswo1209.hallymtaxi.common.GlobalVariable
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.common.context
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.pagingSource.FirestorePagingSource
import com.dldmswo1209.hallymtaxi.pagingSource.PAGE_SIZE
import com.dldmswo1209.hallymtaxi.repository.MainRepository
import com.dldmswo1209.hallymtaxi.repository.RoomRepository
import com.dldmswo1209.hallymtaxi.repository.ServerRepository
import com.dldmswo1209.hallymtaxi.ui.welcome.WelcomeActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime

class MainViewModel(
    private val mainRepository: MainRepository,
    private val roomRepository: RoomRepository,
    application: Application
) : AndroidViewModel(application) {
    private val fireStore = Firebase.firestore
    private val auth = Firebase.auth

    private var userListener: ListenerRegistration? = null
    private var myRoomListener: ListenerRegistration? = null

    private var _startPoint = MutableLiveData<ResultSearchKeyword>()
    val startPoint: LiveData<ResultSearchKeyword> = _startPoint

    private var _endPoint = MutableLiveData<ResultSearchKeyword>()
    val endPoint: LiveData<ResultSearchKeyword> = _endPoint

    private var _isCreated = MutableLiveData<CarPoolRoom>()
    val isCreated: LiveData<CarPoolRoom> = _isCreated

    private var _isJoined = MutableLiveData<Boolean>()
    val isJoined: LiveData<Boolean> = _isJoined

    private var _chatList = MutableLiveData<List<Chat>>()
    val chatList : LiveData<List<Chat>> = _chatList

    private var _roomInfo = MutableLiveData<RoomInfo>()
    val roomInfo : LiveData<RoomInfo> = _roomInfo

    private var _roomHistory = MutableLiveData<List<RoomInfo>>()
    val roomHistory : LiveData<List<RoomInfo>> = _roomHistory

    fun detachRoomPaging(genderOption: String) : Flow<PagingData<CarPoolRoom>> {
        val query = fireStore.collection("Room")
            .whereEqualTo("closed", false) // 이미 마감한 방 필터링
            .whereIn("genderOption", listOf(genderOption, GENDER_OPTION_NONE)) // 성별 옵션에 부합하지 않는 방 필터링
            .whereGreaterThanOrEqualTo("departureTime", LocalDateTime.now().toString()) // 출발시간이 이미 지난 방 필터링
            .orderBy("departureTime", Query.Direction.ASCENDING)// 출발 시간 기준 오름차순
            .orderBy("created", Query.Direction.DESCENDING) // 방 생성 기준 내림차순
            .limit(PAGE_SIZE.toLong())

        return Pager(
            PagingConfig(
                pageSize = PAGE_SIZE
            )
        ){
            FirestorePagingSource(query)
        }.flow.cachedIn(viewModelScope)

    }

    fun logout(activity: Activity, uid: String) {
        myRoomListenerRemove()
        userListenerRemove()

        val userInfo = mapOf<String, Any>(
            "fcmToken" to ""
        )
        CoroutineScope(Dispatchers.IO).launch {
            fireStore.collection("User").document(uid).update(userInfo).await()

            auth.signOut()
            activity.finish()
            activity.startActivity(Intent(activity, WelcomeActivity::class.java))
        }
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

        if (auth.currentUser == null) return null
        getFcmToken()
        fireStore.collection("User").document(auth.currentUser!!.uid).get().addOnSuccessListener {
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

        if (auth.currentUser == null) return null
        getFcmToken()
        userListener = fireStore.collection("User").document(auth.currentUser!!.uid).addSnapshotListener { value, error->
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
            _isCreated.value = it
        }
    }

    fun joinRoom(room: CarPoolRoom, user: User) {
        mainRepository.joinRoom(room, user).observeForever {
            _isJoined.value = it
        }
    }


    suspend fun sendMessage(chat: Chat, userName: String, receiveTokens: List<String?>) {
        mainRepository.sendMessage(chat, userName, receiveTokens)
    }

    fun subscribeMyRoom(user: User) : LiveData<CarPoolRoom?> {
        val room = MutableLiveData<CarPoolRoom?>()

        myRoomListener = fireStore.collection("Room").whereArrayContains("participants", user)
            .addSnapshotListener { value, error ->
                if(error != null){
                    Log.d("testt", "getMyRoom: ${error}")
                    return@addSnapshotListener
                }
                value?.let {querySnapshot ->
                    if(!querySnapshot.isEmpty) {
                        val snapshot = querySnapshot.first()
                        room.postValue(snapshot.toObject())
                        Log.d("testt", "subscribeMyRoom: ${room.value}")
                    }else{
                        room.postValue(null) // 참여중인 방이 없음 null 반환
                    }
                }
            }

        return room
    }


    fun exitRoom(user: User, room: CarPoolRoom) {
        mainRepository.exitRoom(user, room)
    }

    fun deactivateRoom(roomId: String) {
        mainRepository.deactivateRoom(roomId)
    }

    fun insertRoomInfo(roomInfo: RoomInfo) = viewModelScope.launch(Dispatchers.IO) {
        Log.d("testt", "roomInfo 저장!: ${roomInfo}")
        roomRepository.insertRoomInfo(roomInfo)
    }

    fun detachRoomInfo(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
        _roomInfo.postValue(roomRepository.detachRoomInfo(roomId))
    }

    fun updateRoomInfo(roomInfo: RoomInfo) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.updateRoomInfo(roomInfo)
    }

    fun detachRoomInfoHistory() = viewModelScope.launch(Dispatchers.IO) {
        _roomHistory.postValue(roomRepository.detachRoomInfoHistory())
    }

    fun userListenerRemove(){
        userListener?.remove()

    }

    fun myRoomListenerRemove(){
        myRoomListener?.remove()
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
        if(auth.currentUser == null) return
        mainRepository.updateFcmToken(auth.currentUser!!.uid, token)
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
        userListenerRemove()
        myRoomListenerRemove()
    }

}