package com.dldmswo1209.hallymtaxi.data.repository

import android.util.Log
import com.dldmswo1209.hallymtaxi.data.model.VerifyInfo
import com.dldmswo1209.hallymtaxi.data.remote.MainServerApi
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_INVALID_CODE
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_INVALID_EMAIL
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_NO_REQUEST_USER
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_SERVER_ERROR
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_TIME_OUT
import com.dldmswo1209.hallymtaxi.util.ServerResponse.MESSAGE_VERIFY_SUCCESS
import com.dldmswo1209.hallymtaxi.util.ServerResponse.STATUS_FAIL
import com.dldmswo1209.hallymtaxi.util.ServerResponse.STATUS_OK
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.UnknownHostException
import kotlin.math.log

class ServerRepositoryImpl(
    private val client: MainServerApi,
) : ServerRepository {

    override suspend fun sendVerifyMail(email: String, result: (UiState<VerifyInfo>) -> Unit) {
        try{
            val verifyInfo = client.sendVerifyMail(email)
            when(verifyInfo.status) {
                STATUS_OK -> {
                    result.invoke(
                        UiState.Success(verifyInfo)
                    )
                }
                STATUS_FAIL -> {
                    result.invoke(
                        UiState.Failure(MESSAGE_INVALID_EMAIL)
                    )
                }
            }
        } catch (e: Exception) {
            result.invoke(UiState.Failure(MESSAGE_SERVER_ERROR))
        }
    }

    override suspend fun requestVerify(
        email: String,
        code: String,
        result: (UiState<VerifyInfo>) -> Unit
    ) {
        try{
          val verifyInfo = client.requestVerify(email, code)
          when(verifyInfo.status) {
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
        } catch (e: Exception) {
            result.invoke(
                UiState.Failure(MESSAGE_SERVER_ERROR)
            )
        }
    }


    override suspend fun sendPushMessage(
        token: String,
        id: String,
        roomId: String,
        userId: String,
        userName: String,
        message: String,
        messageType: String,
        target: String,
        result: (UiState<String>) -> Unit
    ) {
        if(id.isEmpty()) {
            result.invoke(
                UiState.Success(id)
            )
            return
        }

        try{
            val sent = client.sendPushMessage(
                token = token,
                id = id,
                roomId = roomId,
                userId = userId,
                userName = userName,
                message = message,
                messageType = messageType,
                target = target
            )

            if(sent){
                result.invoke(UiState.Success(id))
            } else {
                result.invoke(UiState.Failure(id))
            }
        } catch (e: Exception) {
            result.invoke(UiState.Failure(id))
        }

    }
}