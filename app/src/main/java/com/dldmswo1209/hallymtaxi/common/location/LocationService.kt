package com.dldmswo1209.hallymtaxi.common.location

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.util.Keys.KAKAO_APP_KEY
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapReverseGeoCoder

class LocationService(private val activity: Activity) {
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private var _address = MutableLiveData<String>()
    val address : LiveData<String> = _address

    companion object{
        private const val PERMISSION_REQUEST = 99
        private var permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private var API_KEY = KAKAO_APP_KEY
    }

    private val reverseGeocodeListener = object: MapReverseGeoCoder.ReverseGeoCodingResultListener {
        override fun onReverseGeoCoderFoundAddress(
            p0: MapReverseGeoCoder?,
            resultAddress: String?,
        ) {
            resultAddress?.let {
                _address.value = it
            }
        }

        override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {
            _address.value = ""
        }
    }

    // 현재 위도 경도, 주소 가져오기
    @SuppressLint("MissingPermission")
    fun getCurrentAddress() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                location?.let {
                    val mapPoint = MapPoint.mapPointWithGeoCoord(it.latitude, it.longitude)
                    reverseGeocorder(mapPoint)
                } ?: kotlin.run {
                    _address.value = ""
                }
            }
    }

    fun reverseGeocorder(mapPoint: MapPoint?) {
        val reverseGeoCoder =
            MapReverseGeoCoder(API_KEY, mapPoint, reverseGeocodeListener, activity)
        reverseGeoCoder.startFindingAddress()
    }


    // 위치 권한 확인
    fun isPermitted(): Boolean {
        MainActivity.permissions.forEach {
            if (ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    fun requestPermission(){
        if (!isPermitted()) {
            ActivityCompat.requestPermissions((activity as MainActivity), permissions, PERMISSION_REQUEST)
        }
    }

}