package com.dldmswo1209.hallymtaxi.retrofit

import com.dldmswo1209.hallymtaxi.data.model.ResultSearchKeyword
import com.dldmswo1209.hallymtaxi.util.Keys.API_KEY
import com.dldmswo1209.hallymtaxi.util.Keys.KAKAO_BASE_URL
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
        fun create() : KakaoApiClient{
            return Retrofit.Builder()
                .baseUrl(KAKAO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(KakaoApiClient::class.java)
        }

    }
}