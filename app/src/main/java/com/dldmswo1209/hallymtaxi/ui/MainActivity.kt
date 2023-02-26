package com.dldmswo1209.hallymtaxi.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.data.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.databinding.ActivityMainBinding
import com.dldmswo1209.hallymtaxi.util.FireStoreTable
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var navController : NavController? = null
    private val checkNetwork by lazy{
        CheckNetwork(this)
    }
    var isNetworkActivate = false
    private var user: User? = null
    private lateinit var myApplication: MyApplication
    private var room: CarPoolRoom? = null

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
        myApplication = application as MyApplication

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

        viewModel.subscribeMyRoom.observe(this){room->
            myApplication.setMyRoom(room)
            this.room = room
        }

        viewModel.subscribeUser.observe(this){ user ->
            myApplication.setUser(user)
            room?.let { pool ->
                // 유저 정보 업데이트 시 현재 참여 중인 방의 내 정보 동기화
                if(pool.roomId.isNotBlank()) viewModel.updateRoomParticipantsInfo(pool.roomId, pool.participants, user)
            }
        }

        user?.let {
            viewModel.subscribeMyRoom(it)
        }
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

    override fun onResume() {
        super.onResume()
        checkNetwork.registerNetworkListener()
    }

    override fun onPause() {
        super.onPause()
        checkNetwork.unRegisterNetworkListener()
    }

}