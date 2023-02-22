package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.ResultSearchKeyword
import com.dldmswo1209.hallymtaxi.data.remote.KakaoApi
import com.dldmswo1209.hallymtaxi.data.UiState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KakaoRepositoryImpl(
    private val client: KakaoApi
) : KakaoRepository {
    override fun searchKeyword(keyword: String, result: (UiState<ResultSearchKeyword>) -> Unit) {
        client.getSearchKeyword(query = keyword).enqueue(object: Callback<ResultSearchKeyword>{
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                response.body()?.let { resultSearchKeyword->
                    result.invoke(
                        UiState.Success(filteredDocument(resultSearchKeyword))
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

    private fun filteredDocument(resultSearchKeyword: ResultSearchKeyword) : ResultSearchKeyword{
        val placeList = resultSearchKeyword.documents
        placeList.forEach {
            if (it.road_address_name.isBlank()) it.road_address_name = it.address_name
        }
        resultSearchKeyword.documents = placeList

        return resultSearchKeyword
    }
}