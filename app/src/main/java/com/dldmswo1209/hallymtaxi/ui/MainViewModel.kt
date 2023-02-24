package com.dldmswo1209.hallymtaxi.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dldmswo1209.hallymtaxi.common.context
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.data.repository.*
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.remote.FirestorePagingSource
import com.dldmswo1209.hallymtaxi.data.remote.PAGE_SIZE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fireStoreRepository: FireStoreRepository,
    private val databaseRepository: DatabaseRepository,
    private val authRepository: AuthRepository,
    private val kakaoRepository: KakaoRepository,
    private val serverRepository: ServerRepository,
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    application: Application
) : AndroidViewModel(application) {

    private var userListener: ListenerRegistration? = null
    private var myRoomListener: ListenerRegistration? = null

    private var _user = MutableLiveData<UiState<User>>()
    val user : LiveData<UiState<User>> = _user

    private var _logout = MutableLiveData<UiState<String>>()
    val logout : LiveData<UiState<String>> = _logout

    private var _updateToken = MutableLiveData<UiState<String>>()
    val updateToken : LiveData<UiState<String>> = _updateToken

    private var _startPoint = MutableLiveData<UiState<ResultSearchKeyword>>()
    val startPoint: LiveData<UiState<ResultSearchKeyword>> = _startPoint

    private var _endPoint = MutableLiveData<UiState<ResultSearchKeyword>>()
    val endPoint: LiveData<UiState<ResultSearchKeyword>> = _endPoint

    private var _createRoom = MutableLiveData<UiState<CarPoolRoom>>()
    val createRoom: LiveData<UiState<CarPoolRoom>> = _createRoom

    private var _joinRoom = MutableLiveData<UiState<String>>()
    val joinRoom: LiveData<UiState<String>> = _joinRoom

    private var _roomInfo = MutableLiveData<RoomInfo>()
    val roomInfo : LiveData<RoomInfo> = _roomInfo

    private var _chatList = MutableLiveData<List<Chat>>()
    val chatList : LiveData<List<Chat>> = _chatList

    private var _sendPush = MutableLiveData<UiState<String>>()
    val sendPush: LiveData<UiState<String>> = _sendPush

    private var _roomHistory = MutableLiveData<List<RoomInfo>>()
    val roomHistory : LiveData<List<RoomInfo>> = _roomHistory

    private var _exitRoom = MutableLiveData<UiState<String>>()
    val exitRoom : LiveData<UiState<String>> = _exitRoom

    private var _deactivateRoom = MutableLiveData<UiState<String>>()
    val deactivateRoom : LiveData<UiState<String>> = _deactivateRoom

    private var _favorites = MutableLiveData<List<Place>>()
    val favorites : LiveData<List<Place>> = _favorites

    private var _updateUserName = MutableLiveData<UiState<String>>()
    val updateUserName : LiveData<UiState<String>> = _updateUserName

    fun getUserInfo(){
        authRepository.getUserInfo { _user.postValue(it) }
    }

    fun subscribeUser() : LiveData<User>? {
        val user = MutableLiveData<User>()

        if (auth.currentUser == null) return null
        userListener = fireStore.collection(FireStoreTable.USER).document(auth.currentUser!!.uid)
            .addSnapshotListener { value, error->
                if(error != null){
                    return@addSnapshotListener
                }
                value?.let {snapshot->
                    user.value = snapshot.toObject<User>()
                }
            }

        return user
    }

    fun logout(uid: String) {
        _logout.postValue(UiState.Loading)
        authRepository.logoutUser(uid){ _logout.postValue(it) }
    }

    fun updateUserName(newName: String) {
        authRepository.updateUserName(newName) { _updateUserName.postValue(it) }
    }

    fun updateFcmToken() {
        fireStoreRepository.updateFcmToken{
            _updateToken.postValue(it)
        }
    }

    fun searchKeyword(keyword: String, isStartPoint: Boolean) = viewModelScope.launch {
        try {
            if (isStartPoint) {
                _startPoint.postValue(UiState.Loading)
                kakaoRepository.searchKeyword(keyword){ _startPoint.postValue(it) }
            }
            else {
                _endPoint.postValue(UiState.Loading)
                kakaoRepository.searchKeyword(keyword){ _endPoint.postValue(it) }
            }
        } catch (e: Exception) {
            networkErrorMessage(e)
        }
    }

    fun detachRoomPaging(genderOption: String) : Flow<PagingData<CarPoolRoom>> {
        val query = fireStore.collection(FireStoreTable.ROOM)
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

    fun createRoom(room: CarPoolRoom, user: User) {
        _createRoom.postValue(UiState.Loading)
        fireStoreRepository.createRoom(room, user){ _createRoom.postValue(it) }
    }

    fun joinRoom(room: CarPoolRoom, user: User) {
        _joinRoom.postValue(UiState.Loading)
        fireStoreRepository.joinRoom(room, user){ _joinRoom.postValue(it) }
    }

    fun subscribeMyRoom(user: User) : LiveData<CarPoolRoom?> {
        val room = MutableLiveData<CarPoolRoom?>()

        myRoomListener = fireStore.collection(FireStoreTable.ROOM)
            .whereArrayContains("participants", user)
            .addSnapshotListener { value, error ->
                if(error != null){
                    return@addSnapshotListener
                }
                value?.let {querySnapshot ->
                    if(!querySnapshot.isEmpty) {
                        val snapshot = querySnapshot.first()
                        room.postValue(snapshot.toObject(CarPoolRoom::class.java))
                    }else{
                        room.postValue(null) // 참여중인 방이 없음 null 반환
                    }
                }
            }

        return room
    }


    fun exitRoom(user: User, room: CarPoolRoom) {
        _exitRoom.postValue(UiState.Loading)
        fireStoreRepository.exitRoom(user, room){ _exitRoom.postValue(it) }
    }

    fun deactivateRoom(roomId: String) {
        _deactivateRoom.postValue(UiState.Loading)
        fireStoreRepository.deactivateRoom(roomId){ _deactivateRoom.postValue(it) }
    }

    fun sendMessage(chat: Chat, userName: String, receiveTokens: List<String?>) {
        _sendPush.postValue(UiState.Loading)
        if(receiveTokens.isEmpty()) chat.sendSuccess = SEND_STATE_SUCCESS
        saveChat(chat)

        receiveTokens.forEachIndexed { index, token->
            if(!token.isNullOrEmpty()){
                serverRepository.sendPushMessage(token, chat.roomId, chat.userId, userName, chat.msg, chat.messageType, chat.id){
                    if(receiveTokens.last() == token) _sendPush.postValue(it)
                }
            }
        }
    }

    fun saveChat(chat: Chat) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.saveChat(chat)
        if(chat.messageType != CHAT_EXIT) {
            databaseRepository.insertRoomInfo(RoomInfo(chat.roomId, chat.msg, chat.dateTime, false, isActivate = true))
        }
    }

    fun updateChatById(id: String, sendSuccess: String) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.updateChatById(id, sendSuccess)
    }

    fun deleteChat(id: String) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.deleteChat(id)
    }

    fun insertRoomInfo(roomInfo: RoomInfo) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.insertRoomInfo(roomInfo)
    }

    fun detachRoomInfo(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
        _roomInfo.postValue(databaseRepository.detachRoomInfo(roomId))
    }

    fun updateRoomInfo(roomInfo: RoomInfo) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.updateRoomInfo(roomInfo)
    }

    fun detachRoomInfoHistory() = viewModelScope.launch(Dispatchers.IO) {
        _roomHistory.postValue(databaseRepository.detachRoomInfoHistory())
    }

    fun detachChatList(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
        _chatList.postValue(databaseRepository.detachChatList(roomId))
    }

    fun getFavorites() = viewModelScope.launch(Dispatchers.IO) {
        _favorites.postValue(databaseRepository.getFavorites())
    }

    fun saveFavorite(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.saveFavorite(place)
    }

    fun deleteFavorite(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.deleteFavorite(place)
    }

    private fun networkErrorMessage(e: Exception){
        Toast.makeText(this@MainViewModel.context, "네트워크 연결상태를 확인해주세요", Toast.LENGTH_SHORT)
            .show()
        e.printStackTrace()
    }

    private fun userListenerRemove(){
        userListener?.remove()
    }

    private fun myRoomListenerRemove(){
        myRoomListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("testt", "onCleared: ")
        userListenerRemove()
        myRoomListenerRemove()
    }

}