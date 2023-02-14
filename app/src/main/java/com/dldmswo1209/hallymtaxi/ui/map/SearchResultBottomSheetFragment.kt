package com.dldmswo1209.hallymtaxi.ui.map

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dldmswo1209.hallymtaxi.common.BottomSheetBehaviorSetting
import com.dldmswo1209.hallymtaxi.databinding.FragmentSearchResultBottomSheetBinding
import com.dldmswo1209.hallymtaxi.data.model.Place
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchResultBottomSheetFragment(
    private val searchList: List<Place>,
    private val onClickPlace: (Place) -> Unit,
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSearchResultBottomSheetBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetBehaviorSetting.bottomSheetBehaviorSetting(requireContext(), theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchResultBottomSheetBinding.inflate(inflater, container, false)
        val filteredSearchList = mutableListOf<Place>()
        val adapter = SearchResultListAdapter {
            onClickPlace(it)
            dialog?.dismiss()
        }

        searchList.forEach {
            if (it.road_address_name.isNotBlank()) filteredSearchList.add(it)
        }
        binding.resultRecyclerView.adapter = adapter.apply {
            if (filteredSearchList.isEmpty()) {
                binding.tvNoResult.visibility = View.VISIBLE
            } else {
                submitList(filteredSearchList)
            }
        }
        return binding.root
    }

}