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
        val adapter = SearchResultListAdapter(onClick = {
            onClickPlace(it)
            dialog?.dismiss()
        })

        binding.resultRecyclerView.adapter = adapter.apply {
            if (searchList.isEmpty()) {
                binding.tvNoResult.visibility = View.VISIBLE
            } else {
                submitList(searchList)
            }
        }
        return binding.root
    }

}