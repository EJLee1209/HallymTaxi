package com.dldmswo1209.hallymtaxi.ui

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.data.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.databinding.ActivityMainBinding
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var navController : NavController? = null
    private val checkNetwork by lazy{
        CheckNetwork(this)
    }
    var isNetworkActivate = false
    private lateinit var myApplication: MyApplication
    private var room: CarPoolRoom? = null
    private lateinit var sharedPreferences : SharedPreferences

    //권한 가져오기
    companion object{
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        const val PERMISSION_REQUEST_CODE = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        requestPermission()
        setObserver()
        bottomNavigationSetup()
//        requestReview()
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        myApplication = application as MyApplication
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE) // 위치권한 요청하기
    }
    private fun setObserver(){
        checkNetwork.isConnected.observe(this){
            isNetworkActivate = it
        }

        viewModel.subscribeMyRoom.observe(this){room->
            myApplication.setMyRoom(room)
            this.room = room
        }

        viewModel.subscribeUser.observe(this){ user ->
            myApplication.setUser(user)
        }

        viewModel.monitoring.observe(this) {
            if(it.deviceId != getDeviceId()) {
                // 다른 기기로 강제 로그인 시도함
                val forcedLoggedInDialog = CustomDialog(
                    title = "시스템 메세지",
                    content = "다른 기기에서 로그인을 했습니다\n잠시 후 앱이 종료됩니다",
                    positiveCallback = { finish() },
                )
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.Default) {
                        forcedLoggedInDialog.show(supportFragmentManager, forcedLoggedInDialog.tag)
                        delay(3000)
                    }
                    finish()
                }

            }
        }
        viewModel.monitoringLoggedIn()

        viewModel.subscribeMyRoom()
        viewModel.subscribeUser()
    }

    private fun bottomNavigationSetup() {
        binding.navigationMain.itemIconTintList = null
        navController =
            supportFragmentManager.findFragmentById(R.id.container_main)?.findNavController()
        navController?.let {
            binding.navigationMain.setupWithNavController(it)
            it.addOnDestinationChangedListener { _, destination, _ ->
                // 바텀 네비게이션이 표시되는 Fragment
                if(destination.id == R.id.navigation_map || destination.id == R.id.navigation_menu
                    || destination.id == R.id.navigation_history){
                    binding.navigationMain.visibility = View.VISIBLE
                }
                // 바텀 네비게이션이 표시되지 않는 Fragment
                else{
                    binding.navigationMain.visibility = View.GONE
                }
            }
        }
    }

//    private fun requestReview() {
//        sharedPreferences = getSharedPreferences("requestReview", MODE_PRIVATE)
//        val lastRequestReview = sharedPreferences.getString("lastRequestReview", null)
//        lastRequestReview?.let {
//            val currentDate = LocalDate.now().toString().dateToFormattedDate()
//            val lastRequest = it.dateToFormattedDate()
//
//            if (currentDate != null && lastRequest != null) {
//                val diff = TimeService.calcDateDiff(currentDate, lastRequest)
//                if (diff >= 10) viewModel.requestReview(this)
//            }
//
//        } ?: kotlin.run {
//            sharedPreferences.edit().apply {
//                val currentDate = LocalDate.now().toString()
//                putString("lastRequestReview", currentDate)
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        checkNetwork.registerNetworkListener()
    }

    override fun onPause() {
        super.onPause()
        checkNetwork.unRegisterNetworkListener()
    }

}