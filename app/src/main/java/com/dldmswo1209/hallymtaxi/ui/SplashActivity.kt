package com.dldmswo1209.hallymtaxi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog.Companion.checkNetworkDialog
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.ui.welcome.WelcomeActivity
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.defaultFavorites
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val checkNetwork by lazy {
        CheckNetwork(this)
    }
    private lateinit var myApplication: MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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
                    myApplication.setUser(state.data)
                    val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                        putExtra("userInfo", state.data)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }

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