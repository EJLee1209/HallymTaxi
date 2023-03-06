package com.dldmswo1209.hallymtaxi.ui.map

import android.app.AlertDialog
import android.content.Context
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MarkerEventListener(
    val context: Context,
    val onClickSetStart: () -> Unit,
    val onClickSetEnd: () -> Unit,
) : MapView.POIItemEventListener {
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        // 마커 클릭
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, poiItem: MapPOIItem?) {}

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        poiItem: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        // 말풍선 클릭시
        poiItem?.let { item ->
            val builder = AlertDialog.Builder(context)
            val itemList = arrayOf("출발지로 설정", "목적지로 설정", "취소")
            val placeName = item.itemName.split("/")[0]

            builder.setTitle(placeName)
            builder.setItems(itemList) { dialog, which ->
                when(which) {
                    0 -> onClickSetStart()
                    1 -> onClickSetEnd()
                    2 -> dialog.dismiss()
                }
            }
            builder.show()
        }


    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        // 마커 드래그
        // 마커 속성 중 isDraggable = true 로 설정 해야 적용됨
    }
}