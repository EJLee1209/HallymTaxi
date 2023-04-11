package com.dldmswo1209.hallymtaxi.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.common.dp
import com.dldmswo1209.hallymtaxi.common.keyboard.KeyboardUtils
import com.dldmswo1209.hallymtaxi.common.location.LocationService
import com.dldmswo1209.hallymtaxi.common.toast
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentMapBinding
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import com.dldmswo1209.hallymtaxi.ui.carpool.PoolListBottomSheetFragment
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import net.daum.mf.map.api.*
import net.daum.mf.map.api.MapView.MapViewEventListener


@AndroidEntryPoint
class MapFragment : Fragment(), MapViewEventListener{
    private lateinit var binding: FragmentMapBinding
    private lateinit var favoritesListAdapter: FavoritesListAdapter
    private lateinit var myApplication: MyApplication
    private lateinit var mapView: MapView
    private lateinit var mapBrain: MapBrain

    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
    }
    private val viewModel : MainViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setObservers()
        setEditorActionListener()
    }
    private fun init(){
        binding.fragment = this

        mapView = MapView(requireActivity())
        mapBrain = MapBrain(fragment = this, binding = binding, mapView = mapView)
        mapView.apply {
            setMapViewEventListener(this@MapFragment)
            setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater))
            setPOIItemEventListener(mapBrain.markerEventListener)
        }
        binding.mapview.addView(mapView)

        mapBrain.locationService = LocationService(requireActivity())
        mapBrain.moveCamera(place_hallym_univ.y, place_hallym_univ.x, 2f)

        myApplication = requireActivity().application as MyApplication

        favoritesListAdapter = FavoritesListAdapter { place ->
            mapBrain.isTab = false
            if (binding.etStartPoint.isFocused) {
                mapBrain.searchResultClickEvent(place, true)
            } else {
                if(mapBrain.startPlace == null) mapBrain.locationService?.getCurrentAddress()
                mapBrain.searchResultClickEvent(place, false)
            }
        }
        binding.rvFavorites.adapter = favoritesListAdapter
    }

    private fun setObservers(){
        myApplication.myRoom.observe(viewLifecycleOwner){ room->

            if(room != null && room.roomId.isNotBlank()){
                binding.room = room
                myApplication.setMyRoomId(room.roomId)
                binding.viewCurrentMyRoom.visibility = View.VISIBLE

                mapBrain.constraintDynamicChange(
                    startId = binding.logoKakao.id,
                    startSide = ConstraintSet.BOTTOM,
                    endId = binding.viewCurrentMyRoom.id,
                    ConstraintSet.TOP,
                    2.dp
                )
                mapBrain.constraintDynamicChange(
                    startId = binding.btnViewPoolList.id,
                    startSide = ConstraintSet.BOTTOM,
                    endId = binding.viewCurrentMyRoom.id,
                    ConstraintSet.TOP,
                    10.dp
                )
            }else{
                binding.viewCurrentMyRoom.visibility = View.GONE

                mapBrain.constraintDynamicChange(
                    startId = binding.logoKakao.id,
                    startSide = ConstraintSet.BOTTOM,
                    endId = binding.parentConstraintLayout.id,
                    ConstraintSet.BOTTOM,
                    92.dp
                )

                mapBrain.constraintDynamicChange(
                    startId = binding.btnViewPoolList.id,
                    startSide = ConstraintSet.BOTTOM,
                    endId = binding.parentConstraintLayout.id,
                    ConstraintSet.BOTTOM,
                    102.dp
                )
            }
            mapBrain.joinedRoom = room
        }

        viewModel.startPoint.observe(viewLifecycleOwner){ state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    toast(state.error ?: "출발지 검색 실패")
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    val placeList = state.data.documents
                    if(mapBrain.isStartPointSearching){ // 출발지를 검색한 경우
                        mapBrain.showSearchResultBottomSheet(placeList, true)
                    }else{ // 그냥 현재 위치를 가져온 경우
                        if(placeList.isNotEmpty()){
                            mapBrain.searchResultClickEvent(placeList.first(), true) // 검색결과 첫번째 장소를 전달
                        }else{
                            // 출발지 주소 검색 실패
                            toast("현재 주소를 찾지 못했습니다")
                        }
                    }
                    mapBrain.isStartPointSearching = false

                }
            }
        }

        viewModel.endPoint.observe(viewLifecycleOwner){ state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    toast(state.error ?: "목적지 검색 실패")
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    val placeList = state.data.documents
                    if(mapBrain.isSearching){
                        mapBrain.showSearchResultBottomSheet(placeList, false)
                    }
                    mapBrain.isSearching = false
                }
            }
        }
        mapBrain.locationService?.address?.observe(viewLifecycleOwner){
            if(it.isNotBlank()){
                viewModel.searchKeyword(it, true) // 출발지를 키워드로 검색
            }
        }

        viewModel.joinRoom.observe(viewLifecycleOwner){state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    toast(state.error ?: "알 수 없는 오류")
                }
                is UiState.Success ->{
                    // 채팅방 입장
                    loadingDialog.dismiss()
                    mapBrain.joinedRoom?.let { room->
                        val action = MapFragmentDirections.actionNavigationMapToChatRoomFragment(room)
                        findNavController().navigate(action)
                    }
                }
            }
        }

        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            favoritesListAdapter.submitList(favorites)
        }
    }

    private fun setEditorActionListener(){
        binding.etInitSearch.setOnEditorActionListener { view, actionId, keyEvent ->
            // 목적지 키워드로 주소를 검색
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                searchAddressFromKeyword(view.text.toString(), isStartPoint = false, getCurrentAddress = true)
            }
            true
        }

        binding.etStartPoint.setOnEditorActionListener { view, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                searchAddressFromKeyword(view.text.toString(), isStartPoint = true, getCurrentAddress = false)
            }
            true
        }

        binding.etEndPoint.setOnEditorActionListener { view, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                searchAddressFromKeyword(view.text.toString(), isStartPoint = false, getCurrentAddress = false)
            }
            true
        }
    }

    private fun searchAddressFromKeyword(keyword: String, isStartPoint: Boolean, getCurrentAddress: Boolean){
        if(keyword.isNotBlank()){
            if(isStartPoint){
                mapBrain.isStartPointSearching = true
                viewModel.searchKeyword(keyword,true)
            }else{
                mapBrain.isSearching = true
                viewModel.searchKeyword(keyword,false)
            }
            if(getCurrentAddress) mapBrain.getCurrentAddress()
        }
    }

    fun onClickViewMyCurrentRoom(){
        mapBrain.joinedRoom?.let { room->
            viewModel.joinRoom(room)
        }
    }

    fun onClickSearchIcon() {
        if (binding.etStartPoint.isFocused) {
            val keyword = binding.etStartPoint.text.toString()
            searchAddressFromKeyword(keyword, isStartPoint = true, getCurrentAddress = false)
        } else {
            val keyword = binding.etEndPoint.text.toString()
            searchAddressFromKeyword(keyword, isStartPoint = false, getCurrentAddress = false)
        }
    }

    fun onClickInitSearchIcon() {
        val keyword = binding.etInitSearch.text.toString()
        searchAddressFromKeyword(keyword, isStartPoint = false, getCurrentAddress = true)
    }

    fun onClickViewPoolListButton() {
        mapBrain.showCarPoolListBottomSheet()
    }

    override fun onResume() {
        super.onResume()

        mapBrain.endPlace?.let {
            mapBrain.searchResultClickEvent(it, false)
        }
        viewModel.getFavorites()
    }

    override fun onPause() {
        super.onPause()
        mapBrain.searchResultBottomSheet?.dialog?.dismiss()
    }

    override fun onMapViewInitialized(p0: MapView?) {}

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {}

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {}

    override fun onMapViewSingleTapped(p0: MapView?, mapPoint: MapPoint) {
        val mapPointGeoCord = mapPoint.mapPointGeoCoord
        if((mapPointGeoCord.latitude == mapBrain.startPlace?.y && mapPointGeoCord.longitude == mapBrain.startPlace?.x) ||
            mapPointGeoCord.latitude == mapBrain.endPlace?.y && mapPointGeoCord.longitude == mapBrain.endPlace?.x) {
            return
        }
        if(mapBrain.startPlace != null && mapBrain.endPlace != null){
            clearStartEndPlace()
        }

        mapBrain.isTab = true
        mapBrain.locationService?.reverseGeocorder(mapPoint)
    }

    private fun clearStartEndPlace() {
        mapBrain.clearStartEndPlace()
        mapView.removeAllPOIItems()
        binding.etEndPoint.text.clear()
        binding.etStartPoint.text.clear()
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {}

    override fun onMapViewLongPressed(p0: MapView?, mapPoint: MapPoint) {}

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {}

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {}

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {}


}