package com.dldmswo1209.hallymtaxi.model

data class Chat(
    var chat_key: String = "",
    val userInfo: User = User(),
    val msg: String = "",
    val dateTime: String = "",
    val joinMsg: Boolean = false,
    val exitMsg: Boolean = false
)
