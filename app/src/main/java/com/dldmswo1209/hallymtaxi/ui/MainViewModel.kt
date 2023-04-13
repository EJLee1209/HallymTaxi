package com.dldmswo1209.hallymtaxi.ui

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.data.repository.*
import com.dldmswo1209.hallymtaxi.data.UiState
import com.google.android.play.core.appupdate.AppUpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    private var _subscribeParticipantsTokens = MutableLiveData<Map<String,String>>()
    val subscribeParticipantsTokens : LiveData<Map<String,String>> = _subscribeParticipantsTokens

    private var _carPoolList = MutableLiveData<UiState<List<CarPoolRoom>>>()
    val carPoolList : LiveData<UiState<List<CarPoolRoom>>> = _carPoolList

    private var _findUserName = MutableLiveData<UiState<String>>()
    val findUserName: LiveData<UiState<String>> = _findUserName

    private var _getParticipantsTokens = MutableLiveData<UiState<Map<String,String>>>()
    val getParticipantsTokens : LiveData<UiState<Map<String,String>>> = _getParticipantsTokens

    private var _monitoring = MutableLiveData<SignedIn>()
    val monitoring : LiveData<SignedIn> = _monitoring

    private var _logout = MutableLiveData<UiState<String>>()
    val logout : LiveData<UiState<String>> = _logout

    private var _deleteAccount = MutableLiveData<UiState<String>>()
    val deleteAccount : LiveData<UiState<String>> = _deleteAccount

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

    // 유저 데이터 구독
    fun subscribeUser() = viewModelScope.launch {
        fireStoreRepository.subscribeUser().collect { user ->
            _subscribeUser.value = user
        }
    }

    // 채팅방의 모든 참여자 fcm token 구독
    fun subscribeParticipantsTokens(roomId: String) = viewModelScope.launch {
        fireStoreRepository.subscribeParticipantsTokens(roomId).collect { tokens ->
            _subscribeParticipantsTokens.postValue( tokens)
        }
    }

    // get 유저 이름
    fun findUserName(uid: String) = viewModelScope.launch {
        fireStoreRepository.findUserName(uid) {
            _findUserName.postValue(it)
        }
    }

    // 채팅방의 모든 참여자 fcm token 가져오기(채팅방 입장시 입장 메세지 보내기 위한 일회용임)
    fun getParticipantsTokens(roomId: String) = viewModelScope.launch {
        _getParticipantsTokens.postValue(UiState.Loading)
        fireStoreRepository.getParticipantsTokens(roomId) {
            _getParticipantsTokens.postValue(it)
        }
    }

    // 현재 내 계정에 로그인하는 사람이 있는지 감시
    fun monitoringLoggedIn() = viewModelScope.launch {
        fireStoreRepository.monitoringLoggedIn().collect{
            _monitoring.postValue(it)
        }
    }

    // 로그아웃
    fun logout() {
        _logout.postValue(UiState.Loading)
        authRepository.logoutUser{ _logout.postValue(it) }
    }

    // 계정 탈퇴
    fun deleteAccount() {
        _deleteAccount.postValue(UiState.Loading)
        authRepository.deleteAccount { _deleteAccount.postValue(it) }
    }

    // fcmtoken 업데이트
    fun updateFcmToken() {
        fireStoreRepository.updateFcmToken{
            _updateToken.postValue(it)
        }
    }

    // 키워드로 주소 검색
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

    // 모든 카풀 목록 가져오기
    fun getAllRoom(genderOption: String) {
        fireStoreRepository.getAllRoom(genderOption) {
            _carPoolList.postValue(it)
        }
    }

    // 카풀 생성
    fun createRoom(room: CarPoolRoom) {
        _createRoom.postValue(UiState.Loading)
        fireStoreRepository.createRoom(room){ _createRoom.postValue(it) }
    }

    // 카풀 참여
    fun joinRoom(room: CarPoolRoom) {
        _joinRoom.postValue(UiState.Loading)
        fireStoreRepository.joinRoom(room){ _joinRoom.postValue(it) }
    }

    // 참여 중인 카풀 구독
    fun subscribeMyRoom() = viewModelScope.launch {
        fireStoreRepository.subscribeMyRoom().collect {
            _subscribeMyRoom.value = it
        }
    }

    // 카풀 퇴장
    fun exitRoom(room: CarPoolRoom) {
        _exitRoom.postValue(UiState.Loading)
        fireStoreRepository.exitRoom(room){ _exitRoom.postValue(it) }
    }

    // 카풀 마감하기
    fun deactivateRoom(roomId: String) {
        _deactivateRoom.postValue(UiState.Loading)
        fireStoreRepository.deactivateRoom(roomId){ _deactivateRoom.postValue(it) }
    }

    // 메세지 전송
    suspend fun sendMessage(chat: Chat, userName: String, receiveTokens: Map<String,String>) = viewModelScope.launch(Dispatchers.IO) {
        _sendPush.postValue(UiState.Loading)
        if(receiveTokens.isEmpty()) chat.sendSuccess = SEND_STATE_SUCCESS
        saveChat(chat)

        var count = 0
        receiveTokens.forEach { (token, platform) ->
            serverRepository.sendPushMessage(
                token = token,
                id = chat.id,
                roomId = chat.roomId,
                userId = chat.userId,
                userName = userName,
                message = chat.msg,
                messageType = chat.messageType,
                target = platform
            ) {

                count++
                if(count == receiveTokens.size) {
                    _sendPush.postValue(it)
                }
            }
        }
    }

    // 메세지 저장
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

    // 메세지 상태 업데이트 (로딩 -> 전송완료 or 전송 실패)
    fun updateChatById(id: String, result: String) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.updateChatById(id, result)
    }

    // 메세지 삭제
    fun deleteChat(id: String) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.deleteChat(id)
    }

    // 채팅방 정보 데이터 저장
    fun insertRoomInfo(roomInfo: RoomInfo) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.insertRoomInfo(roomInfo)
    }

    // 채팅방 정보 가져오기
    fun detachRoomInfo(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
        _roomInfo.postValue(databaseRepository.detachRoomInfo(roomId))
    }

    // 채팅방 정보 업데이트
    fun updateRoomInfo(roomInfo: RoomInfo) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.updateRoomInfo(roomInfo)
    }

    // 히스토리 가져오기
    fun detachRoomInfoHistory() = viewModelScope.launch(Dispatchers.IO) {
        _roomHistory.postValue(databaseRepository.detachRoomInfoHistory())
    }

    // 히스토리 채팅방 메세지 가져오기
    fun detachChatList(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
        _chatList.postValue(databaseRepository.detachChatList(roomId))
    }

    // 즐겨찾기 가져오기
    fun getFavorites() = viewModelScope.launch(Dispatchers.IO) {
        _favorites.postValue(databaseRepository.getFavorites())
    }

    // 즐겨찾기 저장
    fun saveFavorite(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.saveFavorite(place)
    }

    // 즐겨찾기 삭제
    fun deleteFavorite(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.deleteFavorite(place)
    }

    // 모든 테이블 삭제
    fun clearAllTables() = viewModelScope.launch(Dispatchers.IO) {
        async {
            databaseRepository.clearAllTables()
        }.join()
        defaultFavorites.forEach { place ->
            saveFavorite(place)
        }
    }

    // 업데이트 확인
    fun checkAppUpdate() {
        _inAppUpdate.postValue(UiState.Loading)
        inAppRepository.checkAppUpdate { _inAppUpdate.postValue(it) }
    }

    fun requestReview(activity: Activity) {
        inAppRepository.requestReview(activity)
    }

}