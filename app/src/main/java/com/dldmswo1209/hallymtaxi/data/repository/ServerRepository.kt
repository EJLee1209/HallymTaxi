package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.data.UiState

interface ServerRepository {
    suspend fun sendVerifyMail(email: String, result: (UiState<VerifyInfo>) -> Unit)
    suspend fun requestVerify(email: String, code: String, result: (UiState<VerifyInfo>) -> Unit)
    suspend fun sendPushMessage(
        token: String,
        id: String,
        roomId: String,
        userId: String,
        userName: String,
        message: String,
        messageType: String,
        target: String,
        result: (UiState<String>) -> Unit
    )
}