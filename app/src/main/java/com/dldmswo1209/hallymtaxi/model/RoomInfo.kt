package com.dldmswo1209.hallymtaxi.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RoomInfo(
    var roomId: String = "",
    var lastMsg: String = "", // 가장 최근에 받은 메세지
    var lastReceiveMsgDateTime: String = "", // 가장 최근에 메세지를 받은 시간
    var lastChatKey: String = "", // 가장 최근에 받은 메세지의 key (새로운 메세지가 왔는지 확인하기 위함)
    val startPlace: Place = Place(),
    val endPlace: Place = Place(),
): Parcelable