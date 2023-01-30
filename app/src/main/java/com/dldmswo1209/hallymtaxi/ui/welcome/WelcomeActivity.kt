package com.dldmswo1209.hallymtaxi.ui.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel


class WelcomeActivity : AppCompatActivity() {
    val checkNetwork by lazy{
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