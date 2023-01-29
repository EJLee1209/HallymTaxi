package com.dldmswo1209.hallymtaxi.repository

import com.dldmswo1209.hallymtaxi.retrofit.MainServerApiClient

class WelcomeRepository {
    private val client = MainServerApiClient.create()
    suspend fun sendVerifyMail(email: String) = client.sendVerifyMail(email)
    suspend fun requestVerify(email: String, code: String) = client.requestVerify(email, code)
    suspend fun confirmVerified(email: String) = client.confirmVerified(email)
}