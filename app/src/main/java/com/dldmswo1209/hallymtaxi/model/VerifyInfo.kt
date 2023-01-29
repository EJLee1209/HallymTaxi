package com.dldmswo1209.hallymtaxi.model

data class VerifyInfo(
    val status: String = "",
    val message: String = "",
    val data: VerifyData = VerifyData(false)
)

data class VerifyData(
    val verified: Boolean
)

const val STATUS_OK = "SUCCESS"
const val STATUS_FAIL = "FAILED"