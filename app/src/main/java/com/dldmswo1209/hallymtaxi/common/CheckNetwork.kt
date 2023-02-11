package com.dldmswo1209.hallymtaxi.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class CheckNetwork(val context: Context) {
    private var _isConnected = MutableLiveData<Boolean>()
    val isConnected : LiveData<Boolean> = _isConnected

    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // 네트워크가 연결될 때 호출됩니다.
            _isConnected.postValue(true)
        }

        override fun onLost(network: Network) {
            // 네트워크가 끊길 때 호출됩니다.
            _isConnected.postValue(false)
        }
    }
    private var connectivityManager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java)

    fun registerNetworkListener(){
        connectivityManager.registerDefaultNetworkCallback(networkCallBack)
    }

    fun unRegisterNetworkListener(){
        connectivityManager.unregisterNetworkCallback(networkCallBack)
    }

    fun getCurrentNetworkHasTransport() : Boolean{
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }


}