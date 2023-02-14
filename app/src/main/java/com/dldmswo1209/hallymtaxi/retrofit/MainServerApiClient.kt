package com.dldmswo1209.hallymtaxi.retrofit

import com.dldmswo1209.hallymtaxi.data.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.util.Keys.BASE_URL
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MainServerApiClient {
    // 인증 요청 생성
    @POST("api/email/create")
    fun sendVerifyMail(
        @Query("email") email: String = ""
    ): Call<VerifyInfo>

    // 인증 요청
    @POST("api/email/verify")
    fun requestVerify(
        @Query("email") email: String,
        @Query("code") code: String
    ): Call<VerifyInfo>

    // 인증 상태 확인

    // 푸시 메세지
    @POST("/api/message/push")
    fun sendPushMessage(
        @Query("token") token: String,
        @Query("id") id: String,
        @Query("roomId") roomId: String,
        @Query("userId") userId: String,
        @Query("userName") userName: String,
        @Query("message") message: String,
        @Query("messageType") messageType: String,
    ) : Call<Boolean>

}