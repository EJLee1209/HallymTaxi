package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.ResultSearchKeyword
import com.dldmswo1209.hallymtaxi.retrofit.KakaoApiClient
import com.dldmswo1209.hallymtaxi.util.UiState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KakaoRepositoryImpl(
    private val client: KakaoApiClient
) : KakaoRepository {
    override fun searchKeyword(keyword: String, result: (UiState<ResultSearchKeyword>) -> Unit) {
        client.getSearchKeyword(query = keyword).enqueue(object: Callback<ResultSearchKeyword>{
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                response.body()?.let { resultSearchKeyword->
                    result.invoke(
                        UiState.Success(resultSearchKeyword)
                    )
                } ?: kotlin.run {
                    result.invoke(
                        UiState.Failure("키워드 검색 실패")
                    )
                }

            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                result.invoke(
                    UiState.Failure("키워드 검색 실패")
                )
            }

        })

    }
}