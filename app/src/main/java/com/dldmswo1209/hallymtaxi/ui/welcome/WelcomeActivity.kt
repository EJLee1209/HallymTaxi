package com.dldmswo1209.hallymtaxi.ui.welcome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity() {
    private val checkNetwork by lazy{
        CheckNetwork(this)
    }
    var isNetworkActivate = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        checkNetwork.isConnected.observe(this){
            isNetworkActivate = it
        }
    }

    override fun onPause() {
        super.onPause()
        checkNetwork.unRegisterNetworkListener()
    }

    override fun onResume() {
        super.onResume()
        checkNetwork.registerNetworkListener()
    }
}