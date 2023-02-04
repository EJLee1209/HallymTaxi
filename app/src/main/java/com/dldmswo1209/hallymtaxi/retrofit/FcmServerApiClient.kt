package com.dldmswo1209.hallymtaxi.retrofit

import com.dldmswo1209.hallymtaxi.private_key.BASE_URL
import com.google.api.AnnotationsProto.http
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmServerApiClient {

    // 푸시 메세지 요청
    @POST("/push")
    suspend fun sendPushMessage(
        @Body pushBody: PushBody
    )

    companion object{
        fun create() : FcmServerApiClient {
            return Retrofit.Builder()
                .baseUrl("http://192.168.219.105:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FcmServerApiClient::class.java)
        }
    }
}

data class PushBody(
    var token: String, // 메세지를 받는 사람의 fcm 토큰
    val id: String, // 주키
    val roomId: String,
    val userId: String, // 보내는 사람 uid
    val userName: String, // 보내는 사람 이름
    val message: String, // 메세지
    val messageType: String // 입장/퇴장/일반
)

