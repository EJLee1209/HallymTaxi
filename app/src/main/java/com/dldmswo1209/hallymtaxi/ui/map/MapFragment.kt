package com.dldmswo1209.hallymtaxi.ui.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.common.LocationService
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.databinding.FragmentMapBinding
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.ui.carpool.PoolListBottomSheetFragment
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import net.daum.mf.map.api.CameraPosition
import net.daum.mf.map.api.CameraUpdateFactory
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint


class MapFragment: Fragment() {

    private lateinit var binding: FragmentMapBinding
    private lateinit var locationService : LocationService
    private val viewModel : MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private var isSearching = false
    private var isStartPointSearching = false
    private var startPlaceMarker = MapPOIItem()
    private var endPlaceMarker = MapPOIItem()
    private var startPlace : Place? = null
    private var endPlace: Place? = null
    private val hallym_lat : Double = 37.88728582472663
    private val hallym_lng : Double = 127.73812631862366
    private var searchResultBottomSheet : SearchResultBottomSheetFragment? = null
    private var poolListBottomSheet : PoolListBottomSheetFragment? = null
    private var joinedRoom: CarPoolRoom? = null
    private lateinit var user : User

    companion object{
        const val SEARCH_RESULT_BOTTOM_SHEET_TAG = "SearchResultBottomSheetFragment"
        const val POOL_LIST_BOTTOM_SHEET_TAG = "PoolListBottomSheetFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setObservers()
        setEditorActionListener()
        onShortCutButtonClickListener()

    }

    private fun onShortCutButtonClickListener(){

        binding.apply {
            btnShortCutChuncheonStation.setOnClickListener {
            locationService.getCurrentAddress()
            searchResultClickEvent(place_chuncheon_station, false)
        }

            btnShortCutHallym.setOnClickListener {
                locationService.getCurrentAddress()
                searchResultClickEvent(place_hallym_univ, false)
            }

            btnShortCutMyeongDong.setOnClickListener {
                locationService.getCurrentAddress()
                searchResultClickEvent(place_myeoungdong, false)
            }
            btnShortCutBusTerminal.setOnClickListener {
                locationService.getCurrentAddress()
                searchResultClickEvent(place_terminal, false)
            }

            btnShortCutKangwonUniv.setOnClickListener {
                locationService.getCurrentAddress()
                searchResultClickEvent(place_kangwon_univ, false)
            }
        }

    }

    private fun init(){
        locationService = LocationService(requireActivity())
        moveCamera(hallym_lat, hallym_lng, 2f)
        user = (activity as MainActivity).detachUserInfo()

        binding.fragment = this
    }

    private fun setObservers(){
        viewModel.startPoint.observe(viewLifecycleOwner){ result->
            val placeList = result.documents

            if(isStartPointSearching){ // 출발지를 검색한 경우
                showBottomSheetDialog(placeList, isStartPoint = true, isSearchResult = true, tag = SEARCH_RESULT_BOTTOM_SHEET_TAG)
            }else{ // 그냥 현재 위치를 가져온 경우
                if(placeList.isNotEmpty()){
                    searchResultClickEvent(placeList.first(), true) // 검색결과 첫번째 장소를 전달
                }else{
                    // 출발지 주소 검색 실패
                    Toast.makeText(requireContext(), "현재 주소를 찾지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            }
            isStartPointSearching = false
        }

        viewModel.endPoint.observe(viewLifecycleOwner){ result->
            val placeList = result.documents
            if(isSearching){
                showBottomSheetDialog(placeList, isStartPoint = false, isSearchResult = true, tag = SEARCH_RESULT_BOTTOM_SHEET_TAG)
            }
            isSearching = false
        }
        locationService.address.observe(viewLifecycleOwner){
            if(it.isNotBlank()){
                viewModel.searchKeyword(it, true) // 출발지를 키워드로 검색
            }
        }

        viewModel.poolList.observe(viewLifecycleOwner){ roomList->
            roomList.forEach { room->
                if(room.user1?.uid == user.uid || room.user2?.uid == user.uid ||
                    room.user3?.uid == user.uid || room.user4?.uid == user.uid ){
                    // 내가 속한 방이 존재
                    binding.viewCurrentMyRoom.visibility = View.VISIBLE
                    binding.room = room
                    joinedRoom = room
                    (activity as MainActivity).joinedRoom = room

                    return@observe
                }
            }
            joinedRoom = null
            (activity as MainActivity).joinedRoom = null
            binding.viewCurrentMyRoom.visibility = View.GONE
        }
        viewModel.isJoined.observe(viewLifecycleOwner){
            if(it){
                // 채팅방 입장
                joinedRoom?.let { room->
                    val action = MapFragmentDirections.actionNavigationMapToChatRoomFragment(room)
                    findNavController().navigate(action)
                }
            }
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

    private fun searchResultClickEvent(place: Place, isStartPoint: Boolean){
        val newMarker = MapPOIItem().apply {
            // 마커 생성
            itemName = place.place_name
            mapPoint = MapPoint.mapPointWithGeoCoord(place.y,place.x)
            markerType = MapPOIItem.MarkerType.BluePin

        }

        if(isStartPoint){
            binding.etStartPoint.setText(place.road_address_name)
            startPlace = place
            binding.mapview.removePOIItem(startPlaceMarker) // 이전 마커 제거
            startPlaceMarker = newMarker
            binding.mapview.addPOIItem(startPlaceMarker) // 마커 추가

        }else{
            binding.etEndPoint.setText(place.road_address_name)
            endPlace = place
            binding.mapview.removePOIItem(endPlaceMarker) // 이전 마커 제거
            endPlaceMarker = newMarker
            binding.mapview.addPOIItem(endPlaceMarker) // 마커 추가
        }
        if(binding.mapview.poiItems.size == 2){ // 출발지, 목적지 모두 입력 완료
            if(startPlace != null && endPlace != null){
                val centerLat = (startPlace!!.y + endPlace!!.y) / 2.0
                val centerLng = (startPlace!!.x + endPlace!!.x) / 2.0
                moveCamera(centerLat, centerLng, 5f)
                binding.mapview.selectPOIItem(endPlaceMarker, true)
                showBottomSheetDialog(isSearchResult = false, tag = POOL_LIST_BOTTOM_SHEET_TAG) // 카풀방 리스트 보여주기
            }
        }else{
            moveCamera(place.y, place.x, 0.5f)
        }

        val constraintLayout: ConstraintLayout = binding.parentConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.connect(
            binding.shortcutLayout.id,
            ConstraintSet.TOP,
            binding.searchLayout.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.applyTo(constraintLayout)

        binding.initSearchLayout.visibility = View.GONE

        binding.searchLayout.visibility = View.VISIBLE
    }

    private fun moveCamera(lat: Double, lng: Double, zoomLevel: Float) {
        val cameraPosition = CameraPosition(MapPoint.mapPointWithGeoCoord(lat,lng), zoomLevel)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        binding.mapview.moveCamera(cameraUpdate)
    }

    // 장소 검색결과 또는 카풀방 리스트 보여주기
    private fun showBottomSheetDialog(documents: List<Place> = listOf(), isStartPoint: Boolean = false, isSearchResult: Boolean, tag: String){
        if(!isOpenBottomSheetFragment(tag)){
            if(isSearchResult) {
                searchResultBottomSheet = SearchResultBottomSheetFragment(documents) { place ->
                    // 장소 클릭 이벤트 처리
                    searchResultClickEvent(place, isStartPoint)
                }
                searchResultBottomSheet?.show(parentFragmentManager, SEARCH_RESULT_BOTTOM_SHEET_TAG)
            }
            else{
                if(startPlace != null && endPlace != null) {
                    val onClickCreateRoom: () -> Unit = {
                        val action =
                            MapFragmentDirections.actionNavigationMapToNavigationCreateRoom(
                                startPlace!!,
                                endPlace!!
                            )
                        findNavController().navigate(action)
                    }
                    val joinCallback: (CarPoolRoom) -> Unit = { room ->
                        // 채팅방 입장
                        val action =
                            MapFragmentDirections.actionNavigationMapToChatRoomFragment(room)
                        findNavController().navigate(action)
                        poolListBottomSheet?.dialog?.dismiss()
                    }
                    poolListBottomSheet =
                        PoolListBottomSheetFragment(onClickCreateRoom, joinCallback, endPlace!!)
                    poolListBottomSheet?.show(parentFragmentManager, POOL_LIST_BOTTOM_SHEET_TAG)
                }
            }
        }
    }

    private fun isOpenBottomSheetFragment(tag: String) : Boolean{
        return parentFragmentManager.findFragmentByTag(tag) != null
    }

    private fun getCurrentAddress(){
        if(locationService.isPermitted()){
            locationService.getCurrentAddress() // 현재 위치 가져오기
        }else{
            locationService.requestPermission() // 권한 요청
        }
    }

    private fun searchAddressFromKeyword(keyword: String, isStartPoint: Boolean, getCurrentAddress: Boolean){
        if(keyword.isNotBlank()){
            if(isStartPoint){
                isStartPointSearching = true
                viewModel.searchKeyword(keyword,true)
            }else{
                isSearching = true
                viewModel.searchKeyword(keyword,false)
            }
            if(getCurrentAddress) getCurrentAddress()
        }
    }

    fun onClickViewMyCurrentRoom(){
        joinedRoom?.let { room->
            viewModel.joinRoom(room, user)
        }
    }


    override fun onResume() {
        super.onResume()

        endPlace?.let {
            searchResultClickEvent(it, false)
        }

        viewModel.detachAllRoom()

    }

    override fun onPause() {
        super.onPause()
        searchResultBottomSheet?.dialog?.dismiss()
    }

}