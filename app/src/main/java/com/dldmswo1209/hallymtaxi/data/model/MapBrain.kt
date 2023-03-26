package com.dldmswo1209.hallymtaxi.data.model

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.dp
import com.dldmswo1209.hallymtaxi.common.location.LocationService
import com.dldmswo1209.hallymtaxi.databinding.FragmentMapBinding
import com.dldmswo1209.hallymtaxi.ui.carpool.PoolListBottomSheetFragment
import com.dldmswo1209.hallymtaxi.ui.map.MapFragmentDirections
import com.dldmswo1209.hallymtaxi.ui.map.MarkerEventListener
import com.dldmswo1209.hallymtaxi.ui.map.SearchResultBottomSheetFragment
import net.daum.mf.map.api.*

class MapBrain(
    var isSearching: Boolean = false,
    var isStartPointSearching: Boolean = false,
    var isTab: Boolean = false,
    var startPlaceMarker: MapPOIItem = MapPOIItem(),
    var endPlaceMarker: MapPOIItem = MapPOIItem(),
    var tabMarker: MapPOIItem = MapPOIItem(),
    var startPlace : Place? = null,
    var endPlace: Place? = null,
    var tabPlace: Place? = null,
    var searchResultBottomSheet : SearchResultBottomSheetFragment? = null,
    var poolListBottomSheet : PoolListBottomSheetFragment? = null,
    var joinedRoom: CarPoolRoom? = null,
    var locationService: LocationService? = null,
    val fragment: Fragment,
    val binding: FragmentMapBinding,
    var mapView: MapView
) {
    init {
        locationService = LocationService(fragment.requireActivity())
    }

    companion object{
        const val SEARCH_RESULT_BOTTOM_SHEET_TAG = "SearchResultBottomSheetFragment"
        const val POOL_LIST_BOTTOM_SHEET_TAG = "PoolListBottomSheetFragment"
    }

    val onClickCreateRoom: () -> Unit = {
        // 채팅방 생성 버튼 클릭 콜백
        val action =
            MapFragmentDirections.actionNavigationMapToNavigationCreateRoom(
                startPlace!!,
                endPlace!!
            )
        fragment.findNavController().navigate(action)
    }
    val joinCallback: (CarPoolRoom) -> Unit = { room ->
        // 채팅방 입장 콜백
        val action =
            MapFragmentDirections.actionNavigationMapToChatRoomFragment(room)
        fragment.findNavController().navigate(action)
        poolListBottomSheet?.dialog?.dismiss()
    }

    val markerEventListener : MarkerEventListener by lazy{
        MarkerEventListener(
            fragment.requireContext(),
            onClickSetStart = {
                tabPlace?.let {
                    startPlace?.let { startPlace->
                        if(startPlace.place_name == it.place_name &&
                            startPlace.address_name == it.address_name){
                            setCameraCenterAllPOIItems()
                            return@MarkerEventListener
                        }
                    }
                    searchResultClickEvent(it, true)
                    mapView.removePOIItem(tabMarker)
                }
            },
            onClickSetEnd = {
                tabPlace?.let {
                    if(startPlace == null) locationService?.getCurrentAddress()

                    endPlace?.let { endPlace->
                        if(endPlace.place_name == it.place_name &&
                            endPlace.address_name == it.address_name){
                            setCameraCenterAllPOIItems()
                            return@MarkerEventListener
                        }
                    }
                    searchResultClickEvent(it, false)
                    mapView.removePOIItem(tabMarker)
                }
            }
        )
    }

    fun searchResultClickEvent(place: Place, isStartPoint: Boolean){
        val newMarker = MapPOIItem().apply {
            // 마커 생성
            itemName = "${place.place_name}/${place.address_name}"
            mapPoint = MapPoint.mapPointWithGeoCoord(place.y,place.x)
            markerType = MapPOIItem.MarkerType.CustomImage
            isCustomImageAutoscale = false
            setCustomImageAnchor(0.5f, 1.0f)
        }

        if(isTab){
            newMarker.customImageResourceId = R.drawable.map_pin
            tabPlace = place
            mapView.removePOIItem(tabMarker)
            tabMarker = newMarker
            mapView.addPOIItem(tabMarker)
            mapView.selectPOIItem(tabMarker, true)
            isTab = false
        } else {
            if(isStartPoint){
                newMarker.customImageResourceId = R.drawable.start_marker
                binding.etStartPoint.setText(place.road_address_name)
                startPlace = place
                mapView.removePOIItem(startPlaceMarker) // 이전 마커 제거
                startPlaceMarker = newMarker
                mapView.addPOIItem(startPlaceMarker) // 마커 추가
            }else{
                newMarker.customImageResourceId = R.drawable.end_marker
                binding.etEndPoint.setText(place.road_address_name)
                endPlace = place
                mapView.removePOIItem(endPlaceMarker) // 이전 마커 제거
                endPlaceMarker = newMarker
                mapView.addPOIItem(endPlaceMarker) // 마커 추가
            }
        }

        if(startPlace != null && endPlace != null){
            setCameraCenterAllPOIItems()
            showCarPoolListBottomSheet()
        }else{
            setCameraCenterAllPOIItems()
        }

        constraintDynamicChange(
            startId = binding.rvFavorites.id,
            startSide = ConstraintSet.TOP,
            endId = binding.searchLayout.id,
            ConstraintSet.BOTTOM,
            9.dp
        )

        binding.initSearchLayout.visibility = View.GONE
        binding.searchLayout.visibility = View.VISIBLE
    }

    fun constraintDynamicChange(startId: Int, startSide: Int, endId: Int, endSide: Int, dp: Int) {
        val constraintLayout: ConstraintLayout = binding.parentConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.connect(
            startId,
            startSide,
            endId,
            endSide,
            dp
        )

        constraintSet.applyTo(constraintLayout)
    }

    fun moveCamera(lat: Double, lng: Double, zoomLevel: Float) {
        val cameraPosition = CameraPosition(MapPoint.mapPointWithGeoCoord(lat,lng), zoomLevel)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        mapView.moveCamera(cameraUpdate)
    }

    fun setCameraCenterAllPOIItems(){
        mapView.fitMapViewAreaToShowAllPOIItems()
        mapView.zoomOut(true)
        mapView.zoomOut(true)
    }

    fun showSearchResultBottomSheet(searchResults: List<Place>, isStartPoint: Boolean) {
        if(!isOpenBottomSheetFragment(SEARCH_RESULT_BOTTOM_SHEET_TAG)) {
            searchResultBottomSheet = SearchResultBottomSheetFragment(searchResults) { place ->
                // 장소 클릭 이벤트 처리
                searchResultClickEvent(place, isStartPoint)
            }
            searchResultBottomSheet?.show(fragment.parentFragmentManager,
                SEARCH_RESULT_BOTTOM_SHEET_TAG
            )
        }
    }

    private fun showCarPoolListBottomSheet() {
        if(!isOpenBottomSheetFragment(POOL_LIST_BOTTOM_SHEET_TAG)) {
            if(startPlace != null && endPlace != null) {
                poolListBottomSheet = PoolListBottomSheetFragment(onClickCreateRoom, joinCallback, startPlace!!, endPlace!!)
                poolListBottomSheet?.show(fragment.parentFragmentManager,
                    POOL_LIST_BOTTOM_SHEET_TAG
                )
            }
        }
    }

    fun getCurrentAddress() {
        locationService?.let {
            if(it.isPermitted()){
                it.getCurrentAddress() // 현재 위치 가져오기
            }else{
                it.requestPermission() // 권한 요청
            }
        }

    }

    fun clearStartEndPlace() {
        startPlace = null
        endPlace = null
    }

    private fun isOpenBottomSheetFragment(tag: String) : Boolean{
        return fragment.parentFragmentManager.findFragmentByTag(tag) != null
    }



}
