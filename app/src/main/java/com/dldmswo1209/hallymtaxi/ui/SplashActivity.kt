package com.dldmswo1209.hallymtaxi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.data.model.defaultFavorites
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog.Companion.checkNetworkDialog
import com.dldmswo1209.hallymtaxi.ui.welcome.WelcomeActivity
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val checkNetwork by lazy {
        CheckNetwork(this)
    }
    private lateinit var myApplication: MyApplication
    private val UPDATE_REQUEST_CODE = 200
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkAppUpdate()
    }

    private fun checkAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)

        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() === UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) { // 업데이트 있음
                requestUpdate(appUpdateManager, appUpdateInfo)
            } else {
                splashStart()
            }
        }
    }

    private fun requestUpdate(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: AppUpdateInfo
    ) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.IMMEDIATE,
            this,
            UPDATE_REQUEST_CODE
        )
    }

    private fun splashStart() {
        checkFirst()
        init()
        setObserver()
    }


    private fun checkFirst() {
        val sharedPreferences = getSharedPreferences("checkFirst", MODE_PRIVATE)
        val isFirst = sharedPreferences.getBoolean("isFirst", true)

        if (isFirst) {
            sharedPreferences.edit()
                .putBoolean("isFirst", false)
                .apply()

            defaultFavorites.forEach { place ->
                viewModel.saveFavorite(place)
            }
        }
    }

    private fun init() {
        myApplication = application as MyApplication
        if (checkNetwork.getCurrentNetworkHasTransport()) {
            viewModel.updateFcmToken()
        } else {
            checkNetworkDialog(supportFragmentManager)
        }
    }

    private fun setObserver() {
        viewModel.updateToken.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                }
                is UiState.Success -> {
                    viewModel.getUserInfo()
                }
            }
        }
        checkNetwork.isConnected.observe(this) {
            if (it) {
                Toast.makeText(this, "네트워크 연결 성공", Toast.LENGTH_SHORT).show()
                viewModel.updateFcmToken()
            }
        }
        viewModel.user.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                }
                is UiState.Success -> {
                    user = state.data
                    viewModel.getMyRoom(state.data)
                    myApplication.setUser(state.data)
                }
            }
        }
        viewModel.myRoom.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    startMainActivity()
                }
                is UiState.Success -> {
                    val room = state.data
                    viewModel.updateRoomParticipantsInfo(room.roomId, room.participants, user)
                    startMainActivity()
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
            putExtra("userInfo", user)
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        checkNetwork.registerNetworkListener()
    }

    override fun onPause() {
        super.onPause()
        checkNetwork.unRegisterNetworkListener()
    }

}