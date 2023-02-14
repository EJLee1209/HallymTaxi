package com.dldmswo1209.hallymtaxi.data.model

data class VerifyInfo(
    val status: String = "",
    val message: String = "",
    val data: VerifyData = VerifyData(false)
)

data class VerifyData(
    val verified: Boolean = false
)

