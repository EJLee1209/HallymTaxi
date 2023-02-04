package com.dldmswo1209.hallymtaxi.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.CheckNetwork
import com.dldmswo1209.hallymtaxi.common.HashKeyStore
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.common.dateToString
import com.dldmswo1209.hallymtaxi.databinding.ActivityMainBinding
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.ui.map.MapFragmentDirections
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.format.DateTimeFormatterBuilder
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var user: User
    private var navController : NavController? = null
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(application) }
    var joinedRoom: CarPoolRoom? = null
    private val checkNetwork by lazy{
        CheckNetwork(this)
    }
    var isNetworkActivate = false

    //권한 가져오기
    companion object{
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        const val PERMISSION_REQUEST_CODE = 99
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE) // 위치권한 요청하기
        viewModel.getFcmToken()
        setObserver()
        getUserInfo()
        bottomNavigationSetup()

//        val key = HashKeyStore.getKeyHashBase64(this)
//        Log.d("testt", "key : ${key}")
        Log.d("testt", "timestamp: ${Timestamp.now().toDate().dateToString("yyyy-MM-dd HH:mm:ss")}")
    }

    private fun setObserver(){
        checkNetwork.isConnected.observe(this){
            isNetworkActivate = it
        }
    }
    private fun getUserInfo(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            user = intent.getSerializableExtra("userInfo", User::class.java) as User
        }else{
            user = intent.getSerializableExtra("userInfo") as User
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
            }
        }
    }

    fun detachUserInfo() : User{
        return user
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