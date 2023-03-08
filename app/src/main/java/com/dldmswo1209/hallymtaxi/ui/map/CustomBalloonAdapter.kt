package com.dldmswo1209.hallymtaxi.ui.map

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.dldmswo1209.hallymtaxi.R
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem

class CustomBalloonAdapter(
    inflater: LayoutInflater
) : CalloutBalloonAdapter {
    @SuppressLint("InflateParams")
    private val mCalloutBalloon: View = inflater.inflate(R.layout.balloon_layout, null)
    private val name: TextView = mCalloutBalloon.findViewById(R.id.tv_place_name)
    private val address: TextView = mCalloutBalloon.findViewById(R.id.tv_place_address)

    override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
        poiItem?.let { item ->
            val (placeName, placeAddress) = item.itemName.split("/")
            name.text = placeName
            address.text = placeAddress
        }
        return mCalloutBalloon
    }

    override fun getPressedCalloutBalloon(p0: MapPOIItem?): View {
        return mCalloutBalloon
    }
}