package com.dldmswo1209.hallymtaxi.data.repository

import android.app.Activity
import com.dldmswo1209.hallymtaxi.data.UiState
import com.google.android.play.core.appupdate.AppUpdateInfo

interface InAppRepository {
    fun checkAppUpdate(result: (UiState<AppUpdateInfo>) -> Unit)

    fun requestReview(activity: Activity)
}