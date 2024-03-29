package com.dldmswo1209.hallymtaxi.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.dldmswo1209.hallymtaxi.common.getDeviceId
import com.dldmswo1209.hallymtaxi.common.requestUpdate
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.data.model.defaultFavorites
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog.Companion.checkNetworkDialog
import com.dldmswo1209.hallymtaxi.ui.welcome.AuthViewModel
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
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var myApplication: MyApplication
    private var email: String? = ""
    private var password: String? = ""
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        start()

        val extraData = intent.getStringExtra("roomId")
        extraData?.let {
            Log.d("testt", it)
        }

    }

    private fun start() {
        viewModel.inAppUpdate.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    splashStart()
                }
                is UiState.Success -> {
                    requestUpdate(state.data)
                }
            }
        }
        viewModel.checkAppUpdate()
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
        val fromLoginFragment = intent.getBooleanExtra("fromLoginFragment", false)
        if(fromLoginFragment) {
            viewModel.updateFcmToken()
        } else {
            checkLoggedInfo()
        }
    }

    private fun checkLoggedInfo() {
        sharedPreferences = getSharedPreferences("loggedInfo", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", null)
        password = sharedPreferences.getString("password", null)

        if(email != null && password != null) {
            // 로그인 가능 여부 체크
            authViewModel.checkLogged(email!!, getDeviceId())
        }else{
            startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
        }
    }

    private fun setObserver() {
        authViewModel.checkLogged.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    // 로그인 불가능
                    val forceLoginDailog = CustomDialog(
                        "시스템 메세지",
                        content = "다른 기기에서 로그인하여\n자동 로그아웃 처리 되었습니다",
                        positiveCallback = {
                            sharedPreferences.edit().apply {
                                putString("email", null)
                                putString("password", null)
                                apply()
                            }
                            startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                        }
                    )
                    forceLoginDailog.show(supportFragmentManager, forceLoginDailog.tag)
                }
                is UiState.Success -> {
                    // 로그인 가능
                    authViewModel.login(email!!, password!!, getDeviceId())
                }
            }

        }
        authViewModel.login.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    // 로그인 실패
                    startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                }
                is UiState.Success -> {
                    // 로그인 성공
                    viewModel.updateFcmToken()
                }
            }
        }

        viewModel.updateToken.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {}
                is UiState.Failure -> {
                    startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                }
                is UiState.Success -> {
                    myApplication.setFcmToken(state.data)
                    startMainActivity()
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}