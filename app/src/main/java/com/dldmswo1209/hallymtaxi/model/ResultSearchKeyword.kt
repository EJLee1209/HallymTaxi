package com.dldmswo1209.hallymtaxi.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class ResultSearchKeyword(
    var documents: List<Place>
)

@Parcelize
data class Place(
    val place_name: String = "",
    val address_name: String = "",
    val road_address_name: String = "",
    val x: Double = 0.0, // longitude
    val y: Double = 0.0 // latitude
) : java.io.Serializable, Parcelable


// 검색 빈도 수가 높을 것으로 예상되는 장소
val place_chuncheon_station = Place(
    place_name = "춘천역 경춘선",
    address_name = "강원 춘천시 근화동190",
    road_address_name = "강원 춘천시 공지로 591",
    x = 127.716698593345,
    y = 37.884512737384
)
val place_hallym_univ = Place(
    place_name = "한림대학교",
    address_name = "강원 춘천시 후평동 671",
    road_address_name = "강원 춘천시 한림대학길 1",
    x = 127.73812631862366,
    y = 37.88728582472663
)
val place_myeoungdong = Place(
    place_name = "명동입구",
    address_name = "강원 춘천시 조양동 26-8",
    road_address_name = "강원 춘천시 금강로 55-1",
    x = 127.728269223544,
    y = 37.8802289779797
)
val place_terminal = Place(
    place_name = "춘천시외버스터미널",
    address_name = "강원 춘천시 온의동 511",
    road_address_name = "강원 춘천시 경춘로 2341",
    x = 127.718623878218,
    y = 37.8631849670621
)
val place_kangwon_univ = Place(
    place_name = "강원대학교 춘천캠퍼스",
    address_name = "강원 춘천시 효자동 192-1",
    road_address_name = "강원 춘천시 강원대학길 1",
    x = 127.74403884881542,
    y = 37.86945254603451
)