package com.dldmswo1209.hallymtaxi.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class User(
    var uid: String = "",
    val email: String = "",
    val name: String = "",
    val gender: String = "",
    var fcmToken: String = "",
): java.io.Serializable
