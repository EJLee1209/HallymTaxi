package com.dldmswo1209.hallymtaxi.data.repository

import android.app.Activity
import android.content.Context
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.util.ServerResponse
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode

class InAppRepositoryImpl(
    val context: Context
): InAppRepository {
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

    override fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
            } else {
                // There was some problem, log or handle the error code.
            }
        }
    }

}