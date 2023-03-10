package com.dldmswo1209.hallymtaxi.ui.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
    private lateinit var locationService : LocationService
    private lateinit var favoritesListAdapter: FavoritesListAdapter

    private lateinit var myApplication: MyApplication
    private lateinit var mapView: MapView

    private var isSearching = false
    private var isStartPointSearching = false
    private var isTab = false
    private var startPlaceMarker = MapPOIItem()
    private var endPlaceMarker = MapPOIItem()
    private var tabMarker = MapPOIItem()

    private var startPlace : Place? = null
    private var endPlace: Place? = null
    private var tabPlace: Place? = null
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
        // ????????? ?????? ?????? ?????? ??????
        val action =
            MapFragmentDirections.actionNavigationMapToNavigationCreateRoom(
                startPlace!!,
                endPlace!!
            )
        findNavController().navigate(action)
    }
    private val joinCallback: (CarPoolRoom) -> Unit = { room ->
        // ????????? ?????? ??????
        val action =
            MapFragmentDirections.actionNavigationMapToChatRoomFragment(room)
        findNavController().navigate(action)
        poolListBottomSheet?.dialog?.dismiss()
    }

    private val markerEventListener : MarkerEventListener by lazy{
        MarkerEventListener(
            requireContext(),
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
                    if(startPlace == null) locationService.getCurrentAddress()

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

        mapView = MapView(requireActivity()).apply {
            setMapViewEventListener(this@MapFragment)
            setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater))
            setPOIItemEventListener(markerEventListener)
        }
        binding.mapview.addView(mapView)

        locationService = LocationService(requireActivity())
        moveCamera(place_hallym_univ.y, place_hallym_univ.x, 2f)

        myApplication = requireActivity().application as MyApplication

        favoritesListAdapter = FavoritesListAdapter { place ->
            isTab = false
            if (binding.etStartPoint.isFocused) {
                searchResultClickEvent(place, true)
            } else {
                if(startPlace == null) locationService.getCurrentAddress()
                searchResultClickEvent(place, false)
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
                    toast(state.error ?: "????????? ?????? ??????")
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    val placeList = state.data.documents
                    if(isStartPointSearching){ // ???????????? ????????? ??????
                        showSearchResultBottomSheet(placeList, true)
                    }else{ // ?????? ?????? ????????? ????????? ??????
                        if(placeList.isNotEmpty()){
                            searchResultClickEvent(placeList.first(), true) // ???????????? ????????? ????????? ??????
                        }else{
                            // ????????? ?????? ?????? ??????
                            toast("?????? ????????? ?????? ???????????????")
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
                    toast(state.error ?: "????????? ?????? ??????")
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
                viewModel.searchKeyword(it, true) // ???????????? ???????????? ??????
            }
        }

        viewModel.joinRoom.observe(viewLifecycleOwner){state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    toast(state.error ?: "??? ??? ?????? ??????")
                }
                is UiState.Success ->{
                    // ????????? ??????
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
            // ????????? ???????????? ????????? ??????
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
            // ?????? ??????
            itemName = "${place.place_name}/${place.address_name}"
            mapPoint = MapPoint.mapPointWithGeoCoord(place.y,place.x)
            markerType = MapPOIItem.MarkerType.CustomImage
            isCustomImageAutoscale = false
            setCustomImageAnchor(0.5f, 1.0f)
        }

        if(isTab){
            newMarker.markerType = MapPOIItem.MarkerType.BluePin
            tabPlace = place
            mapView.removePOIItem(tabMarker)
            tabMarker = newMarker
            mapView.addPOIItem(tabMarker)
            mapView.selectPOIItem(tabMarker, false)
            isTab = false
        } else {
            if(isStartPoint){
                binding.etStartPoint.setText(place.road_address_name)
                startPlace = place
                mapView.removePOIItem(startPlaceMarker) // ?????? ?????? ??????
                newMarker.customImageResourceId = R.drawable.start_marker
                startPlaceMarker = newMarker
                mapView.addPOIItem(startPlaceMarker) // ?????? ??????
            }else{
                binding.etEndPoint.setText(place.road_address_name)
                endPlace = place
                mapView.removePOIItem(endPlaceMarker) // ?????? ?????? ??????
                newMarker.customImageResourceId = R.drawable.end_marker
                endPlaceMarker = newMarker
                mapView.addPOIItem(endPlaceMarker) // ?????? ??????
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

    // ?????? ???????????? ?????? ????????? ????????? ????????????
    private fun showSearchResultBottomSheet(searchResults: List<Place>, isStartPoint: Boolean) {
        if(!isOpenBottomSheetFragment(SEARCH_RESULT_BOTTOM_SHEET_TAG)) {
            searchResultBottomSheet = SearchResultBottomSheetFragment(searchResults) { place ->
                // ?????? ?????? ????????? ??????
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
            locationService.getCurrentAddress() // ?????? ?????? ????????????
        }else{
            locationService.requestPermission() // ?????? ??????
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

    override fun onMapViewInitialized(p0: MapView?) {}

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {}

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {}

    override fun onMapViewSingleTapped(p0: MapView?, mapPoint: MapPoint) {
        val mapPointGeoCord = mapPoint.mapPointGeoCoord
        if((mapPointGeoCord.latitude == startPlace?.y && mapPointGeoCord.longitude == startPlace?.x) ||
                mapPointGeoCord.latitude == endPlace?.y && mapPointGeoCord.longitude == endPlace?.x) {
            return
        }
        if(startPlace != null && endPlace != null){
            clearStartEndPlace()
        }

        isTab = true
        locationService.reverseGeocorder(mapPoint)
    }

    private fun clearStartEndPlace() {
        startPlace = null
        endPlace = null
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