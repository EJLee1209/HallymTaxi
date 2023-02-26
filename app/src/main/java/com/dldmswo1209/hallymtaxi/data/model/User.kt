package com.dldmswo1209.hallymtaxi.data.model

data class User(
    var uid: String = "",
    val email: String = "",
    var name: String = "",
    val gender: String = "",
    var fcmToken: String = "",
): java.io.Serializable
