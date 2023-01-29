package com.dldmswo1209.hallymtaxi.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
@Parcelize
data class CarPoolRoom(
    var roomId: String = "",
    var user1: User? = null,
    var user2: User? = null,
    var user3: User? = null,
    var user4: User? = null,
    var lastMsg: String = "", // 가장 최근에 받은 메세지
    var lastReceiveMsgDateTime: String = "", // 가장 최근에 메세지를 받은 시간
    var lastChatKey: String = "", // 가장 최근에 받은 메세지의 key (새로운 메세지가 왔는지 확인하기 위함)
    var userMaxCount : Int = 4, // 제한 인원
    var userCount: Int = 1, // 현재 참여 유저 수
    val startPlace: Place = Place(),
    val endPlace: Place = Place(),
    var departureTime: String = "", // 출발 시간
    var created: String = "", // 방 생성 시간
    var genderOption: String = "" // 탑승 옵션(남자/여자/상관없음)
): Parcelable

const val GENDER_OPTION_MALE = "male"
const val GENDER_OPTION_FEMALE = "female"
const val GENDER_OPTION_NONE = "none"
