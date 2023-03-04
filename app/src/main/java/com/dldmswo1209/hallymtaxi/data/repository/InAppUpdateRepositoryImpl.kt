package com.dldmswo1209.hallymtaxi.data.repository

import android.content.Context
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.util.ServerResponse
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateRepositoryImpl(
    val context: Context
): InAppUpdateRepository {
    override fun checkAppUpdate(result: (UiState<AppUpdateInfo>) -> Unit) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)

        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() === UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) { // 업데이트 있음
                result.invoke(UiState.Success(appUpdateInfo))
            } else {
                result.invoke(UiState.Failure(ServerResponse.CHECK_UPDATE_NOTHING))
            }
        }
            .addOnFailureListener {
                result.invoke(UiState.Failure(ServerResponse.CHECK_UPDATE_FAILED))
            }
    }

}