package com.dldmswo1209.hallymtaxi.repository

import com.dldmswo1209.hallymtaxi.retrofit.MainServerApiClient

class ServerRepository {
    private val client = MainServerApiClient.create()
    suspend fun sendVerifyMail(email: String) = client.sendVerifyMail(email)
    suspend fun requestVerify(email: String, code: String) = client.requestVerify(email, code)
    suspend fun confirmVerified(email: String) = client.confirmVerified(email)

    suspend fun sendPushMessage(token: String, roomId: String, userId: String, userName: String, message: String, messageType: String, id: String = "") = client.sendPushMessage(token, id, roomId, userId, userName, message, messageType)
}