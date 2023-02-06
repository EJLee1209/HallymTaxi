package com.dldmswo1209.hallymtaxi.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
@Parcelize
data class CarPoolRoom(
    var roomId: String = "",
    val participants: MutableList<User> = mutableListOf(),
    var userMaxCount : Int = 4, // 제한 인원
    var userCount: Int = 1, // 현재 참여 유저 수
    val startPlace: Place = Place(),
    val endPlace: Place = Place(),
    var departureTime: String = "", // 출발 시간
    var created: String = "", // 방 생성 시간
    var genderOption: String = "", // 탑승 옵션(남자/여자/상관없음)
    var closed: Boolean = false // 방 마감 여부
): Parcelable

const val GENDER_OPTION_MALE = "male"
const val GENDER_OPTION_FEMALE = "female"
const val GENDER_OPTION_NONE = "none"
