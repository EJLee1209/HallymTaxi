package com.dldmswo1209.hallymtaxi.ui.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide.init
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.location.LocationService
import com.dldmswo1209.hallymtaxi.common.toast
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.Place
import com.dldmswo1209.hallymtaxi.data.model.place_hallym_univ
import com.dldmswo1209.hallymtaxi.databinding.FragmentFavoriteMapBinding
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.ui.map.MapFragment
import com.dldmswo1209.hallymtaxi.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.*

@AndroidEntryPoint
class FavoriteMapFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteMapBinding
    private lateinit var mapView: MapView
    private lateinit var locationService : LocationService
    private val viewModel : MainViewModel by viewModels()
    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteMapBinding.inflate(layoutInflater)

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    //하단
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.btnAdd.visibility = View.VISIBLE
                        binding.rvPlaceList.visibility = View.GONE
                    }

                    //다펼처짐
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.btnAdd.visibility = View.GONE
                        binding.rvPlaceList.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setObserver()
        getCurrentAddress()
    }

    private fun init(){
        mapView = MapView(requireActivity())
        binding.mapView.addView(mapView)
        binding.fragment = this

        locationService = LocationService(requireActivity())
    }
    private fun setObserver(){
        viewModel.startPoint.observe(viewLifecycleOwner){ state ->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    // 한림대를 default값으로 설정
                    binding.currentPlace = place_hallym_univ
                    addMarker(place_hallym_univ)
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    val place = state.data.documents.first() // 현재 위치
                    binding.currentPlace = place
                    addMarker(place)
                }
            }
        }

        locationService.address.observe(viewLifecycleOwner){ keyword ->
            viewModel.searchKeyword(keyword, true)
        }

    }

    private fun addMarker(place: Place) {
        val newMarker = MapPOIItem().apply {
            // 마커 생성
            itemName = place.place_name
            mapPoint = MapPoint.mapPointWithGeoCoord(place.y, place.x)
            markerType = MapPOIItem.MarkerType.BluePin
            setCustomImageAnchor(0.5f, 1.0f)
        }
        mapView.addPOIItem(newMarker)
        setCameraCenterAllPOIItems()
    }

    private fun setCameraCenterAllPOIItems(){
        mapView.fitMapViewAreaToShowAllPOIItems()
        mapView.zoomOut(true)
        mapView.zoomOut(true)
    }

    private fun getCurrentAddress(){
        if(locationService.isPermitted()){
            locationService.getCurrentAddress() // 현재 위치 가져오기
        }else{
            locationService.requestPermission() // 권한 요청
        }
    }

    fun onClickBack(){
        findNavController().popBackStack()
    }
}