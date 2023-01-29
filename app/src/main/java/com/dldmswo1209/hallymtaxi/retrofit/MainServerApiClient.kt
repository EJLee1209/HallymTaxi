package com.dldmswo1209.hallymtaxi.retrofit

import com.dldmswo1209.hallymtaxi.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.private_key.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MainServerApiClient {

    // 인증 요청 생성
    @POST("api/email/create")
    suspend fun sendVerifyMail(
        @Query("email") email: String
    ): VerifyInfo

    // 인증 요청
    @POST("api/email/verify")
    suspend fun requestVerify(
        @Query("email") email: String,
        @Query("code") code: String
    ): VerifyInfo

    // 인증 상태 확인
    @GET
    suspend fun confirmVerified(
        @Query("email") email: String
    ): VerifyInfo

    companion object{
        fun create() : MainServerApiClient{
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MainServerApiClient::class.java)
        }

    }
}