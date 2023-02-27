package com.dldmswo1209.hallymtaxi.data.repository

import com.dldmswo1209.hallymtaxi.data.UiState
import com.google.android.play.core.appupdate.AppUpdateInfo

interface InAppUpdateRepository {
    fun checkAppUpdate(result: (UiState<AppUpdateInfo>) -> Unit)
}