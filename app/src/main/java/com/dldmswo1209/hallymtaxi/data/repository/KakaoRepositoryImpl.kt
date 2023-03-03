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
    override suspend fun searchKeyword(keyword: String, result: (UiState<ResultSearchKeyword>) -> Unit) {
        try{
            val searchResult = client.getSearchKeyword(query = keyword)
            result.invoke(
                UiState.Success(filteredDocument(searchResult))
            )
        } catch (e: Exception) {
            result.invoke(
                UiState.Failure("키워드 검색 실패")
            )
        }
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