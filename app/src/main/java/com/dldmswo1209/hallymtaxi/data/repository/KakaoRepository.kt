package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.ResultSearchKeyword
import com.dldmswo1209.hallymtaxi.data.UiState

interface KakaoRepository {
    suspend fun searchKeyword(keyword: String, result: (UiState<ResultSearchKeyword>) -> Unit)
}