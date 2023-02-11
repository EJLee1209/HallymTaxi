package com.dldmswo1209.hallymtaxi.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import com.dldmswo1209.hallymtaxi.common.FirebaseAnalyticsManager
import com.dldmswo1209.hallymtaxi.common.GlobalVariable
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.databinding.ActivityMainBinding
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(application) }

    private var navController : NavController? = null
    private val checkNetwork by lazy{
        CheckNetwork(this)
    }
    var isNetworkActivate = false
    private var user: User? = null
    private lateinit var globalVariable: GlobalVariable
    private lateinit var firebaseAnalytics: FirebaseAnalytics

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        globalVariable = application as GlobalVariable
        firebaseAnalytics = Firebase.analytics

        requestPermission()
        getIntentExtraData()
        setObserver()
        bottomNavigationSetup()

    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE) // 위치권한 요청하기
    }

    private fun getIntentExtraData(){
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("userInfo", User::class.java) as User
        }else{
            intent.getSerializableExtra("userInfo") as User
        }
    }
    private fun setObserver(){
        checkNetwork.isConnected.observe(this){
            isNetworkActivate = it
        }

        viewModel.subscribeUser()?.observe(this){
            globalVariable.setUser(it)
        } ?: kotlin.run {
            startActivity(Intent(this, SplashActivity::class.java))
        }

        user?.let {
            viewModel.subscribeMyRoom(it).observe(this){room->
                globalVariable.setMyRoom(room)
            }
        }

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

                when(destination.id){
                    R.id.navigation_map -> {
                        FirebaseAnalyticsManager.analyticsScreenViewLogEvent("지도를 봤습니다", "MapFragment")
                    }
                    R.id.navigation_menu -> {
                        FirebaseAnalyticsManager.analyticsScreenViewLogEvent("메뉴를 봤습니다", "MenuFragment")
                    }
                    R.id.navigation_history ->{
                        FirebaseAnalyticsManager.analyticsScreenViewLogEvent("히스토리를 봤습니다", "HistoryFragment")
                    }
                    R.id.chatRoomFragment ->{
                        FirebaseAnalyticsManager.analyticsScreenViewLogEvent("채팅방을 봤습니다", "ChatRoomFragment")
                    }
                    R.id.chatRoomHistoryFragment->{
                        FirebaseAnalyticsManager.analyticsScreenViewLogEvent("히스토리 채팅방을 봤습니다", "ChatRoomHistoryFragment")
                    }
                    R.id.navigation_create_room->{
                        FirebaseAnalyticsManager.analyticsScreenViewLogEvent("채팅방 만드는 화면을 봤습니다", "CreateRoomFragment")
                    }

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