package com.dldmswo1209.hallymtaxi.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "room_info")
data class RoomInfo(
    @PrimaryKey
    @ColumnInfo(name = "roomId")
    var roomId: String = "",
    @ColumnInfo(name = "lastMsg")
    var lastMsg: String = "", // 가장 최근에 받은 메세지
    @ColumnInfo(name = "lastReceiveMsgDateTime")
    var lastReceiveMsgDateTime: String, // 가장 최근에 메세지를 받은 시간
    @ColumnInfo(name = "isNewMessage")
    var isNewMessage: Boolean = true, // 읽지 않은 새 메세지가 왔는지 여부
)