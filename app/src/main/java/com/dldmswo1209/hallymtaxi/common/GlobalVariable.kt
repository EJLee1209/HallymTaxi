package com.dldmswo1209.hallymtaxi.common

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom

class GlobalVariable: Application() {
    private var isViewChatRoom: Boolean = false // 유저가 채팅창을 보고 있음?

    private var _myRoom = MutableLiveData<CarPoolRoom?>() // 현재 참여중인 채팅방
    val myRoom : LiveData<CarPoolRoom?> = _myRoom


    fun getIsViewChatRoom() : Boolean = isViewChatRoom
    fun setIsViewChatRoom(value: Boolean){ isViewChatRoom = value }

    fun setMyRoom(room: CarPoolRoom?) { _myRoom.postValue(room) }


}