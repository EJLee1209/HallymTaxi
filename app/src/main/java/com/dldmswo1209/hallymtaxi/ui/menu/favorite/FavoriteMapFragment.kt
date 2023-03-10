package com.dldmswo1209.hallymtaxi.ui.menu.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.common.clearFocusAndHideKeyboard
import com.dldmswo1209.hallymtaxi.common.location.LocationService
import com.dldmswo1209.hallymtaxi.common.registerBackPressedCallback
import com.dldmswo1209.hallymtaxi.common.toast
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.Place
import com.dldmswo1209.hallymtaxi.data.model.place_hallym_univ
import com.dldmswo1209.hallymtaxi.databinding.FragmentFavoriteMapBinding
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.ui.map.SearchResultListAdapter
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.*

@AndroidEntryPoint
class FavoriteMapFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteMapBinding
    private lateinit var mapView: MapView
    private lateinit var searchResultListAdapter: SearchResultListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var place: Place = place_hallym_univ
    private val viewModel: MainViewModel by viewModels()
    private val loadingDialog by lazy {
        LoadingDialog(requireActivity())
    }
    private val locationService by lazy {
        LocationService(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteMapBinding.inflate(layoutInflater)
        addBottomSheetCallback()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserver()
        init()
        registerBackPressedCallback()
        setEditorActionListener()
    }

    private fun addBottomSheetCallback() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    //??????
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.btnAdd.visibility = View.VISIBLE
                        binding.rvPlaceList.visibility = View.GONE
                    }

                    //????????????
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.btnAdd.visibility = View.GONE
                        binding.rvPlaceList.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })
    }

    private fun init() {
        mapView = MapView(requireActivity())
        binding.mapView.addView(mapView)
        binding.fragment = this

        searchResultListAdapter = SearchResultListAdapter(onClick = { place ->
            binding.currentPlace = place
            this.place = place
            addMarker(place)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        })
        binding.rvPlaceList.adapter = searchResultListAdapter

        val args: FavoriteMapFragmentArgs by navArgs()
        args.place?.let {
            binding.currentPlace = it
            this.place = it
            addMarker(it)
        } ?: kotlin.run {
            getCurrentAddress()
        }
    }

    private fun setObserver() {
        viewModel.startPoint.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    // ???????????? default????????? ??????
                    binding.currentPlace = place_hallym_univ
                    addMarker(place_hallym_univ)
                }
                is UiState.Success -> {
                    loadingDialog.dismiss()
                    val place = state.data.documents.firstOrNull() // ?????? ??????
                    place?.let {
                        binding.currentPlace = it
                        this.place = it
                        addMarker(it)
                    } ?: kotlin.run {
                        binding.currentPlace = place_hallym_univ
                        addMarker(place_hallym_univ)
                    }
                }
            }
        }

        viewModel.endPoint.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                }
                is UiState.Success -> {
                    loadingDialog.dismiss()
                    val placeList = state.data.documents
                    searchResultListAdapter.submitList(placeList)
                    if (placeList.isEmpty()) {
                        binding.layoutNoSearchList.visibility = View.VISIBLE
                        return@observe
                    }
                    binding.layoutNoSearchList.visibility = View.GONE
                }
            }
        }

        locationService.address.observe(viewLifecycleOwner) { keyword ->
            viewModel.searchKeyword(keyword, true)
        }

    }

    private fun addMarker(place: Place) {
        val newMarker = MapPOIItem().apply {
            // ?????? ??????
            itemName = place.place_name
            mapPoint = MapPoint.mapPointWithGeoCoord(place.y, place.x)
            markerType = MapPOIItem.MarkerType.BluePin
            setCustomImageAnchor(0.5f, 1.0f)
        }
        mapView.removeAllPOIItems()
        mapView.addPOIItem(newMarker)
        setCameraCenterAllPOIItems()
    }

    private fun setCameraCenterAllPOIItems() {
        mapView.fitMapViewAreaToShowAllPOIItems()
        mapView.zoomOut(true)
        mapView.zoomOut(true)
    }

    private fun getCurrentAddress() {
        if (locationService.isPermitted()) {
            locationService.getCurrentAddress() // ?????? ?????? ????????????
        } else {
            locationService.requestPermission() // ?????? ??????
        }
    }

    private fun searchFromKeyword() {
        val keyword = binding.etSearch.text.toString()
        if (keyword.isNotBlank()) {
            viewModel.searchKeyword(keyword, false)
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setEditorActionListener() {
        binding.etSearch.setOnEditorActionListener { view, actionId, keyEvent ->
            // ????????? ???????????? ????????? ??????
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFromKeyword()
                binding.etSearch.clearFocusAndHideKeyboard(requireActivity())
            }
            true
        }
    }

    fun onClickOk() {
        // ?????? ??????
        viewModel.saveFavorite(place)
        toast("??????????????? ?????? ????????????.")
    }

    fun onClickBack() {
        findNavController().navigateUp()
    }
}