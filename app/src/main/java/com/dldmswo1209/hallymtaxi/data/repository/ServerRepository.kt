package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.util.UiState

interface ServerRepository {
    fun sendVerifyMail(email: String, result: (UiState<VerifyInfo>) -> Unit)
    fun requestVerify(email: String, code: String, result: (UiState<VerifyInfo>) -> Unit)
    fun sendPushMessage(
        token: String,
        roomId: String,
        userId: String,
        userName: String,
        message: String,
        messageType: String,
        id: String = "",
        result: (UiState<Boolean>) -> Unit
    )
}