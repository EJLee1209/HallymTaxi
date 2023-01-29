package com.dldmswo1209.hallymtaxi.retrofit

import com.dldmswo1209.hallymtaxi.model.ResultSearchKeyword
import com.dldmswo1209.hallymtaxi.private_key.API_KEY
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoApiClient {

    // 카카오 맵 api 키워드로 검색
    @GET("v2/local/search/keyword.json")
    suspend fun getSearchKeyword(
        @Header("Authorization") key: String = API_KEY, // 카카오 api 인증키
        @Query("query") query: String // 검색을 원하는 질의어
    ): ResultSearchKeyword

    companion object{
        private const val KAKAO_BASE_URL = "https://dapi.kakao.com/" // 카카오 api

        fun create() : KakaoApiClient{
            return Retrofit.Builder()
                .baseUrl(KAKAO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(KakaoApiClient::class.java)
        }

    }
}