package com.dldmswo1209.hallymtaxi.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dldmswo1209.hallymtaxi.common.dateToString
import com.google.firebase.Timestamp
import java.util.UUID

@Entity(tableName = "chat")
data class Chat(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: String = UUID.randomUUID().toString(), // primary key
    @ColumnInfo(name = "roomId")
    val roomId: String = "" ,
    @ColumnInfo(name = "userId")
    val userId: String = "", // 보내는 유저 uid
    @ColumnInfo(name = "userName")
    val userName: String = "",
    @ColumnInfo(name = "msg")
    val msg: String = "", // 메세지
    @ColumnInfo(name = "dateTime")
    val dateTime: String = Timestamp.now().toDate().dateToString(), // 보낸 시간
    @ColumnInfo(name = "messageType")
    val messageType: String = "", // 종류(입장/퇴장/일반 메세지)
    @ColumnInfo(name = "sendSuccess")
    var sendSuccess: String = SEND_STATE_LOADING // 메세지 전송 완료 여부
)

const val CHAT_NORMAL = "NORMAL"
const val CHAT_JOIN = "JOIN"
const val CHAT_EXIT = "EXIT"
const val CHAT_ETC = "ETC"

const val SEND_STATE_FAIL = "FAIL"
const val SEND_STATE_SUCCESS = "SUCCESS"
const val SEND_STATE_LOADING = "LOADING"