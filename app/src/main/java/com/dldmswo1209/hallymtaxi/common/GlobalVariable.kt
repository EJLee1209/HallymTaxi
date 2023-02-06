package com.dldmswo1209.hallymtaxi.common

import android.app.Application

class GlobalVariable: Application() {
    var isViewChatRoom: Boolean = false

    fun getIsViewChatRoom() : Boolean = isViewChatRoom
    fun setIsViewChatRoom(value: Boolean){ isViewChatRoom = value }



}