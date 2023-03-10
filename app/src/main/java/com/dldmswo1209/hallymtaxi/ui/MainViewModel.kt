package com.dldmswo1209.hallymtaxi.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.*
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.data.repository.*
import com.dldmswo1209.hallymtaxi.data.UiState
import com.google.android.play.core.appupdate.AppUpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fireStoreRepository: FireStoreRepository,
    private val databaseRepository: DatabaseRepository,
    private val authRepository: AuthRepository,
    private val kakaoRepository: KakaoRepository,
    private val serverRepository: ServerRepository,
    private val inAppRepository: InAppRepository,
    application: Application
) : AndroidViewModel(application) {

    private var _subscribeUser = MutableLiveData<User>()
    val subscribeUser : LiveData<User> = _subscribeUser

    private var _subscribeMyRoom = MutableLiveData<CarPoolRoom>()
    val subscribeMyRoom : LiveData<CarPoolRoom> = _subscribeMyRoom

    private var _subscribeParticipantsTokens = MutableLiveData<List<String>>()
    val subscribeParticipantsTokens : LiveData<List<String>> = _subscribeParticipantsTokens

    private var _carPoolList = MutableLiveData<UiState<List<CarPoolRoom>>>()
    val carPoolList : LiveData<UiState<List<CarPoolRoom>>> = _carPoolList


    private var _findUserName = MutableLiveData<UiState<String>>()
    val findUserName: LiveData<UiState<String>> = _findUserName

    private var _getParticipantsTokens = MutableLiveData<UiState<List<String>>>()
    val getParticipantsTokens : LiveData<UiState<List<String>>> = _getParticipantsTokens

    private var _monitoring = MutableLiveData<SignedIn>()
    val monitoring : LiveData<SignedIn> = _monitoring

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

    private var _inAppUpdate = MutableLiveData<UiState<AppUpdateInfo>>()
    val inAppUpdate : LiveData<UiState<AppUpdateInfo>> = _inAppUpdate

    fun subscribeUser() = viewModelScope.launch {
        fireStoreRepository.subscribeUser().collect { user ->
            _subscribeUser.value = user
        }
    }

    fun subscribeParticipantsTokens(roomId: String) = viewModelScope.launch {
        fireStoreRepository.subscribeParticipantsTokens(roomId).collect { tokens ->
            _subscribeParticipantsTokens.postValue( tokens)
        }
    }

    fun findUserName(uid: String) = viewModelScope.launch {
        fireStoreRepository.findUserName(uid) {
            _findUserName.postValue(it)
        }
    }

    fun getParticipantsTokens(roomId: String) = viewModelScope.launch {
        _getParticipantsTokens.postValue(UiState.Loading)
        fireStoreRepository.getParticipantsTokens(roomId) {
            _getParticipantsTokens.postValue(it)
        }
    }

    fun monitoringLoggedIn() = viewModelScope.launch {
        fireStoreRepository.monitoringLoggedIn().collect{
            _monitoring.postValue(it)
        }
    }

    fun logout() {
        _logout.postValue(UiState.Loading)
        authRepository.logoutUser{ _logout.postValue(it) }
    }

    fun updateFcmToken() {
        fireStoreRepository.updateFcmToken{
            _updateToken.postValue(it)
        }
    }

    fun searchKeyword(keyword: String, isStartPoint: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        if (isStartPoint) {
            _startPoint.postValue(UiState.Loading)
            kakaoRepository.searchKeyword(keyword){ _startPoint.postValue(it) }
        }
        else {
            _endPoint.postValue(UiState.Loading)
            kakaoRepository.searchKeyword(keyword){ _endPoint.postValue(it) }
        }
    }

    fun getAllRoom(genderOption: String) {
        fireStoreRepository.getAllRoom(genderOption) {
            _carPoolList.postValue(it)
        }
    }

    fun createRoom(room: CarPoolRoom) {
        _createRoom.postValue(UiState.Loading)
        fireStoreRepository.createRoom(room){ _createRoom.postValue(it) }
    }

    fun joinRoom(room: CarPoolRoom) {
        _joinRoom.postValue(UiState.Loading)
        fireStoreRepository.joinRoom(room){ _joinRoom.postValue(it) }
    }

    fun subscribeMyRoom() = viewModelScope.launch {
        fireStoreRepository.subscribeMyRoom().collect {
            _subscribeMyRoom.value = it
        }
    }

    fun exitRoom(room: CarPoolRoom) {
        _exitRoom.postValue(UiState.Loading)
        fireStoreRepository.exitRoom(room){ _exitRoom.postValue(it) }
    }

    fun deactivateRoom(roomId: String) {
        _deactivateRoom.postValue(UiState.Loading)
        fireStoreRepository.deactivateRoom(roomId){ _deactivateRoom.postValue(it) }
    }

    suspend fun sendMessage(chat: Chat, userName: String, receiveTokens: List<String?>) = viewModelScope.launch(Dispatchers.IO) {
        _sendPush.postValue(UiState.Loading)
        if(receiveTokens.isEmpty()) chat.sendSuccess = SEND_STATE_SUCCESS
        saveChat(chat)

        receiveTokens.forEachIndexed { index, token->
            if(token != null){
                serverRepository.sendPushMessage(token, chat.roomId, chat.userId, userName, chat.msg, chat.messageType, chat.id){
                    if(receiveTokens.last() == token) _sendPush.postValue(it)
                }
            }
        }
    }

    private fun saveChat(chat: Chat) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.saveChat(chat)
        val roomInfo = RoomInfo(
            chat.roomId,
            chat.msg,
            chat.dateTime,
            false,
            isActivate = true
        )
         if(chat.messageType != CHAT_EXIT) {
             databaseRepository.insertRoomInfo(roomInfo)
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

    fun checkAppUpdate() {
        _inAppUpdate.postValue(UiState.Loading)
        inAppRepository.checkAppUpdate { _inAppUpdate.postValue(it) }
    }

    fun requestReview(activity: Activity) {
        inAppRepository.requestReview(activity)
    }

}