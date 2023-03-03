package com.dldmswo1209.hallymtaxi.data.remote

import com.dldmswo1209.hallymtaxi.data.model.ResultSearchKeyword
import com.dldmswo1209.hallymtaxi.util.Keys.API_KEY
import com.dldmswo1209.hallymtaxi.util.Keys.KAKAO_API_END_POINT
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoApi {
    // 카카오 맵 api 키워드로 검색
    @GET(KAKAO_API_END_POINT)
    suspend fun getSearchKeyword(
        @Header("Authorization") key: String = API_KEY, // 카카오 api 인증키
        @Query("query") query: String // 검색을 원하는 질의어
    ): ResultSearchKeyword

}