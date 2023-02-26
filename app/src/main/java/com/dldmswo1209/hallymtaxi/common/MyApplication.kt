package com.dldmswo1209.hallymtaxi.common

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dldmswo1209.hallymtaxi.data.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.data.model.User
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    private var isViewChatRoom: Boolean = false // 유저가 채팅창을 보고 있음?
    private var _myRoom = MutableLiveData<CarPoolRoom>() // 현재 참여중인 채팅방
    val myRoom : LiveData<CarPoolRoom> = _myRoom

    private var _userLiveData = MutableLiveData<User>() // 현재 참여중인 채팅방
    val userLiveData : LiveData<User> = _userLiveData

    private var myRoomId: String? = null
    private var user : User? = null



    fun getIsViewChatRoom() : Boolean = isViewChatRoom
    fun setIsViewChatRoom(value: Boolean){ isViewChatRoom = value }
    fun setMyRoom(room: CarPoolRoom) { _myRoom.postValue(room) }
    fun getMyRoomId() = myRoomId
    fun setMyRoomId(roomId: String) { myRoomId = roomId }
    fun getUser() : User? = user
    fun setUser(user: User) {
        this.user = user
        _userLiveData.postValue(user)
    }

}