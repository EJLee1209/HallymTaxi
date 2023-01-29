package com.dldmswo1209.hallymtaxi

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.ui.welcome.WelcomeActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    val viewModel: MainViewModel by viewModels { ViewModelFactory(this.application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        viewModel.getUserInfo()?.observe(this){
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("userInfo", it)
            }
            startActivity(intent)
            finish()
        } ?: kotlin.run {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }
    }
}