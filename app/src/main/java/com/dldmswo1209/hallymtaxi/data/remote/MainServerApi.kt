package com.dldmswo1209.hallymtaxi.data.remote

import com.dldmswo1209.hallymtaxi.data.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.util.Keys.API_EMAIL_CREATE
import com.dldmswo1209.hallymtaxi.util.Keys.API_EMAIL_VERIFY
import com.dldmswo1209.hallymtaxi.util.Keys.API_MESSAGE_PUSH
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface MainServerApi {
    // 인증 요청 생성
    @POST(API_EMAIL_CREATE)
    suspend fun sendVerifyMail(
        @Query("email") email: String = ""
    ): VerifyInfo

    // 인증 요청
    @POST(API_EMAIL_VERIFY)
    suspend fun requestVerify(
        @Query("email") email: String,
        @Query("code") code: String
    ): VerifyInfo

    // 푸시 메세지
    @POST(API_MESSAGE_PUSH)
    suspend fun sendPushMessage(
        @Query("token") token: String,
        @Query("id") id: String,
        @Query("roomId") roomId: String,
        @Query("userId") userId: String,
        @Query("userName") userName: String,
        @Query("message") message: String,
        @Query("messageType") messageType: String,
        @Query("target") target: String
    ) : Boolean

}