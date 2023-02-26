package com.dldmswo1209.hallymtaxi.ui.map

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.common.dp
import com.dldmswo1209.hallymtaxi.common.location.LocationService
import com.dldmswo1209.hallymtaxi.common.toast
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentMapBinding
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.ui.carpool.PoolListBottomSheetFragment
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import net.daum.mf.map.api.CameraPosition
import net.daum.mf.map.api.CameraUpdateFactory
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

@AndroidEntryPoint
class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private lateinit var locationService : LocationService
    private lateinit var favoritesListAdapter: FavoritesListAdapter
    private lateinit var user : User
    private lateinit var myApplication: MyApplication
    private lateinit var mapView: MapView

    private var isSearching = false
    private var isStartPointSearching = false
    private var startPlaceMarker = MapPOIItem()
    private var endPlaceMarker = MapPOIItem()
    private var startPlace : Place? = null
    private var endPlace: Place? = null
    private var searchResultBottomSheet : SearchResultBottomSheetFragment? = null
    private var poolListBottomSheet : PoolListBottomSheetFragment? = null
    private var joinedRoom: CarPoolRoom? = null
    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
    }
    private val viewModel : MainViewModel by viewModels()

    companion object{
        const val SEARCH_RESULT_BOTTOM_SHEET_TAG = "SearchResultBottomSheetFragment"
        const val POOL_LIST_BOTTOM_SHEET_TAG = "PoolListBottomSheetFragment"
    }

    private val onClickCreateRoom: () -> Unit = {
        // 채팅방 생성 버튼 클릭 콜백
        val action =
            MapFragmentDirections.actionNavigationMapToNavigationCreateRoom(
                startPlace!!,
                endPlace!!
            )
        findNavController().navigate(action)
    }
    private val joinCallback: (CarPoolRoom) -> Unit = { room ->
        // 채팅방 입장 콜백
        val action =
            MapFragmentDirections.actionNavigationMapToChatRoomFragment(room)
        findNavController().navigate(action)
        poolListBottomSheet?.dialog?.dismiss()
    }

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
        binding.mapview.addView(mapView)

        locationService = LocationService(requireActivity())
        moveCamera(place_hallym_univ.y, place_hallym_univ.x, 2f)

        myApplication = requireActivity().application as MyApplication
        user = myApplication.getUser() ?: kotlin.run {
            startActivity(Intent(requireActivity(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }

        favoritesListAdapter = FavoritesListAdapter { place ->
            locationService.getCurrentAddress()
            searchResultClickEvent(place, false)
        }
        binding.rvFavorites.adapter = favoritesListAdapter
    }

    private fun setObservers(){
        myApplication.userLiveData.observe(viewLifecycleOwner) { user ->
            this.user = user
        }

        myApplication.myRoom.observe(viewLifecycleOwner){ room->

            if(room != null && room.roomId.isNotBlank()){
                binding.room = room
                myApplication.setMyRoomId(room.roomId)
                binding.viewCurrentMyRoom.visibility = View.VISIBLE

                constraintDynamicChange(
                    startId = binding.logoKakao.id,
                    startSide = ConstraintSet.BOTTOM,
                    endId = binding.viewCurrentMyRoom.id,
                    ConstraintSet.TOP,
                    2.dp
                )
            }else{
                binding.viewCurrentMyRoom.visibility = View.GONE

                constraintDynamicChange(
                    startId = binding.logoKakao.id,
                    startSide = ConstraintSet.BOTTOM,
                    endId = binding.parentConstraintLayout.id,
                    ConstraintSet.BOTTOM,
                    92.dp
                )
            }
            joinedRoom = room
        }

        viewModel.startPoint.observe(viewLifecycleOwner){ state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    toast("현재 주소를 찾지 못했습니다")
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    val placeList = state.data.documents
                    if(isStartPointSearching){ // 출발지를 검색한 경우
                        showSearchResultBottomSheet(placeList, true)
                    }else{ // 그냥 현재 위치를 가져온 경우
                        if(placeList.isNotEmpty()){
                            searchResultClickEvent(placeList.first(), true) // 검색결과 첫번째 장소를 전달
                        }else{
                            // 출발지 주소 검색 실패
                            toast("현재 주소를 찾지 못했습니다")
                        }
                    }
                    isStartPointSearching = false

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
                    toast("현재 주소를 찾지 못했습니다")
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    val placeList = state.data.documents
                    if(isSearching){
                        showSearchResultBottomSheet(placeList, false)
                    }
                    isSearching = false
                }
            }
        }
        locationService.address.observe(viewLifecycleOwner){
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
                    toast("현재 주소를 찾지 못했습니다")
                }
                is UiState.Success ->{
                    // 채팅방 입장
                    loadingDialog.dismiss()
                    joinedRoom?.let { room->
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


    private fun searchResultClickEvent(place: Place, isStartPoint: Boolean){
        val newMarker = MapPOIItem().apply {
            // 마커 생성
            itemName = place.place_name
            mapPoint = MapPoint.mapPointWithGeoCoord(place.y,place.x)
            markerType = MapPOIItem.MarkerType.CustomImage
            isCustomImageAutoscale = false
            setCustomImageAnchor(0.5f, 1.0f)
        }

        if(isStartPoint){
            binding.etStartPoint.setText(place.road_address_name)
            startPlace = place
            mapView.removePOIItem(startPlaceMarker) // 이전 마커 제거
            newMarker.customImageResourceId = R.drawable.start_marker
            startPlaceMarker = newMarker
            mapView.addPOIItem(startPlaceMarker) // 마커 추가

        }else{
            binding.etEndPoint.setText(place.road_address_name)
            endPlace = place
            mapView.removePOIItem(endPlaceMarker) // 이전 마커 제거
            newMarker.customImageResourceId = R.drawable.end_marker
            endPlaceMarker = newMarker
            mapView.addPOIItem(endPlaceMarker) // 마커 추가
        }
        if(mapView.poiItems.size == 2){ // 출발지, 목적지 모두 입력 완료
            if(startPlace != null && endPlace != null){
                mapView.selectPOIItem(endPlaceMarker, true)
                setCameraCenterAllPOIItems()
                showCarPoolListBottomSheet()
            }
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

    private fun constraintDynamicChange(startId: Int, startSide: Int, endId: Int, endSide: Int, dp: Int) {
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

    private fun moveCamera(lat: Double, lng: Double, zoomLevel: Float) {
        val cameraPosition = CameraPosition(MapPoint.mapPointWithGeoCoord(lat,lng), zoomLevel)
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        mapView.moveCamera(cameraUpdate)
    }

    private fun setCameraCenterAllPOIItems(){
        mapView.fitMapViewAreaToShowAllPOIItems()
        mapView.zoomOut(true)
        mapView.zoomOut(true)
    }

    // 장소 검색결과 또는 카풀방 리스트 보여주기
    private fun showSearchResultBottomSheet(searchResults: List<Place>, isStartPoint: Boolean) {
        if(!isOpenBottomSheetFragment(SEARCH_RESULT_BOTTOM_SHEET_TAG)) {
            searchResultBottomSheet = SearchResultBottomSheetFragment(searchResults) { place ->
                // 장소 클릭 이벤트 처리
                searchResultClickEvent(place, isStartPoint)
            }
            searchResultBottomSheet?.show(parentFragmentManager, SEARCH_RESULT_BOTTOM_SHEET_TAG)
        }
    }

    private fun showCarPoolListBottomSheet() {
        if(!isOpenBottomSheetFragment(POOL_LIST_BOTTOM_SHEET_TAG)) {
            if(startPlace != null && endPlace != null) {
                poolListBottomSheet =
                    PoolListBottomSheetFragment(onClickCreateRoom, joinCallback, endPlace!!)
                poolListBottomSheet?.show(parentFragmentManager, POOL_LIST_BOTTOM_SHEET_TAG)
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

    override fun onResume() {
        super.onResume()

        endPlace?.let {
            searchResultClickEvent(it, false)
        }
        viewModel.getFavorites()
    }

    override fun onPause() {
        super.onPause()
        searchResultBottomSheet?.dialog?.dismiss()
    }
}