package com.dldmswo1209.hallymtaxi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dldmswo1209.hallymtaxi.common.dateToString
import com.google.firebase.Timestamp

@Entity(tableName = "chat")
data class Chat(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0, // primary key
    @ColumnInfo(name = "roomId")
    val roomId: String = "" ,
    @ColumnInfo(name = "userId")
    val userId: String = "", // 보내는 유저 uid
    @ColumnInfo(name = "msg")
    val msg: String = "", // 메세지
    @ColumnInfo(name = "dateTime")
    val dateTime: String = Timestamp.now().toDate().dateToString(), // 보낸 시간
    @ColumnInfo(name = "messageType")
    val messageType: String = "" // 종류(입장/퇴장/일반 메세지)
)

const val CHAT_NORMAL = "NORMAL"
const val CHAT_JOIN = "JOIN"
const val CHAT_EXIT = "EXIT"