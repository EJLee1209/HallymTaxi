package com.dldmswo1209.hallymtaxi

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import com.dldmswo1209.hallymtaxi.common.CustomDialog
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.ui.welcome.WelcomeActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(this.application) }
    private val checkNetwork by lazy{
        CheckNetwork(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if(checkNetwork.getCurrentNetworkHasTransport()){
            checkLoginInfo()
        }else {
            checkNetworkDialog()
            setObserver()
        }
    }

    private fun checkLoginInfo(){
        viewModel.getUserInfo()?.observe(this@SplashActivity){
            val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                putExtra("userInfo", it)
            }
            startActivity(intent)
            finish()
        } ?: kotlin.run {
            startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
        }
    }

    private fun checkNetworkDialog(){
        val networkDialog = CustomDialog(
            title = "네트워크 연결 실패",
            content = "셀룰러 또는 와이파이를 연결해주세요",
        )
        networkDialog.show(supportFragmentManager, networkDialog.tag)
    }

    private fun setObserver(){
        checkNetwork.isConnected.observe(this) {
            if (it) {
                Toast.makeText(this, "네트워크 연결 성공", Toast.LENGTH_SHORT).show()
                checkLoginInfo()
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