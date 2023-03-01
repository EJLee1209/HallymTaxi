package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.data.remote.MainServerApi
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_INVALID_CODE
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_NO_REQUEST_USER
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_TIME_OUT
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_VERIFY_SUCCESS
import com.dldmswo1209.hallymtaxi.util.ServerResponse.STATUS_FAIL
import com.dldmswo1209.hallymtaxi.util.ServerResponse.STATUS_OK
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServerRepositoryImpl(
    private val client: MainServerApi,
) : ServerRepository {

    override fun sendVerifyMail(email: String, result: (UiState<VerifyInfo>) -> Unit) {
        client.sendVerifyMail(email).enqueue(object : Callback<VerifyInfo> {
            override fun onResponse(call: Call<VerifyInfo>, response: Response<VerifyInfo>) {
                response.body()?.let { verifyInfo ->
                    when (verifyInfo.status) {
                        STATUS_OK -> {
                            result.invoke(
                                UiState.Success(verifyInfo)
                            )
                        }
                        STATUS_FAIL -> {
                            result.invoke(
                                UiState.Failure("인증 메일 전송 실패,\n웹메일을 다시 확인해주세요")
                            )
                        }
                    }
                } ?: kotlin.run {
                    result.invoke(
                        UiState.Failure("서버 점검 중 입니다\n이용에 불편을 드려 죄송합니다")
                    )
                }
            }

            override fun onFailure(call: Call<VerifyInfo>, t: Throwable) {
                result.invoke(
                    UiState.Failure(t.message)
                )
            }

        })
    }

    override fun requestVerify(
        email: String,
        code: String,
        result: (UiState<VerifyInfo>) -> Unit
    ) {
        client.requestVerify(email, code).enqueue(object : Callback<VerifyInfo> {
            override fun onResponse(call: Call<VerifyInfo>, response: Response<VerifyInfo>) {
                response.body()?.let { verifyInfo ->
                    when (verifyInfo.status) {
                        STATUS_OK -> {
                            when (verifyInfo.message) {
                                MESSAGE_VERIFY_SUCCESS -> {
                                    result.invoke(
                                        UiState.Success(verifyInfo)
                                    )
                                }
                                MESSAGE_INVALID_CODE -> {
                                    result.invoke(
                                        UiState.Failure(MESSAGE_INVALID_CODE)
                                    )
                                }
                            }
                        }
                        STATUS_FAIL -> {
                            when (verifyInfo.message) {
                                MESSAGE_NO_REQUEST_USER -> {
                                    result.invoke(
                                        UiState.Failure(MESSAGE_NO_REQUEST_USER)
                                    )
                                }
                                MESSAGE_TIME_OUT -> {
                                    result.invoke(
                                        UiState.Failure(MESSAGE_TIME_OUT)
                                    )
                                }
                            }
                        }
                    }
                } ?: kotlin.run {
                    result.invoke(
                        UiState.Failure("No Response")
                    )
                }


            }

            override fun onFailure(call: Call<VerifyInfo>, t: Throwable) {
                result.invoke(
                    UiState.Failure(t.message)
                )
            }

        })
    }


    override fun sendPushMessage(
        token: String,
        roomId: String,
        userId: String,
        userName: String,
        message: String,
        messageType: String,
        id: String,
        result: (UiState<String>) -> Unit
    ) {
        if(token.isEmpty()) {
            result.invoke(
                UiState.Success(id)
            )
            return
        }

        client.sendPushMessage(token, id, roomId, userId, userName, message, messageType)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    response.body()?.let { sent->
                        if(sent){
                            result.invoke(
                                UiState.Success(id)
                            )
                        }else{
                            result.invoke(
                                UiState.Failure(id)
                            )
                        }

                    }?: kotlin.run {
                        result.invoke(
                            UiState.Failure(id)
                        )
                    }

                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    result.invoke(
                        UiState.Failure(id)
                    )
                }
            })
    }
}