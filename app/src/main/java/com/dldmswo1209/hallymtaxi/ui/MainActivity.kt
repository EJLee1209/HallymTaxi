package com.dldmswo1209.hallymtaxi.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.FilterChip
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.common.keyboard.KeyboardUtils
import com.dldmswo1209.hallymtaxi.data.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.databinding.ActivityMainBinding
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.welcome.WelcomeActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*


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
    private lateinit var sharedPreferences: SharedPreferences
    private var room: CarPoolRoom? = null

    //권한 가져오기
    companion object{
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
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
        sharedPreferences = getSharedPreferences("loggedInfo", Context.MODE_PRIVATE)
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            TedPermission.create()
                .setPermissionListener(object: PermissionListener {
                    override fun onPermissionGranted() {}

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}

                })
                .setDeniedMessage("앱의 기능을 정상적으로 사용하기 위해 모든 권한을 허용해주세요\n\n알림 권한 거부시 메세지를 받지 못할 수도 있습니다")
                .setPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                .check()
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }


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
                    content = "다른 기기에서 로그인을 했습니다\n잠시 후 자동으로 로그아웃 됩니다.",
                    positiveCallback = {
                        forceLogout()
                        viewModel.clearAllTables()
                    },
                )
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.Default) {
                        forcedLoggedInDialog.show(supportFragmentManager, forcedLoggedInDialog.tag)
                        delay(2000)
                    }
                    forceLogout()
                    viewModel.clearAllTables()
                }

            }
        }
        viewModel.monitoringLoggedIn()
        viewModel.subscribeMyRoom()
        viewModel.subscribeUser()
    }

    private fun forceLogout() {
        sharedPreferences.edit().apply {
            putString("email", null)
            putString("password", null)
            apply()
        }
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
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