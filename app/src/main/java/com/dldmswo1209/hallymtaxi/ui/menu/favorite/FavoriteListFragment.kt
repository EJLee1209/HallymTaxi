package com.dldmswo1209.hallymtaxi.ui.menu.favorite

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.registerBackPressedCallback
import com.dldmswo1209.hallymtaxi.databinding.FragmentFavoriteListBinding
import com.dldmswo1209.hallymtaxi.ui.map.SearchResultListAdapter
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class FavoriteListFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteListBinding
    private lateinit var adapter: SearchResultListAdapter
    private var isEditMode: Boolean = false
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        registerBackPressedCallback()
        setObserver()
    }

    private fun init() {
        binding.fragment = this
        adapter = SearchResultListAdapter(onClick = { place ->
            val action = FavoriteListFragmentDirections.actionFavoriteListFragmentToFavoriteMapFragment(place)
            findNavController().navigate(action)
        },
        onClickDelete = { place ->
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Default) {
                    viewModel.deleteFavorite(place)
                    delay(100)
                }
                viewModel.getFavorites()
            }
        })
        binding.rvFavorites.adapter = adapter
    }

    private fun setObserver() {
        viewModel.getFavorites()
        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            adapter.submitList(favorites)
            if(favorites.isEmpty()){
                binding.noFavorite.visibility = View.VISIBLE
            }else{
                binding.noFavorite.visibility = View.GONE
            }
        }

    }

    fun onClickUpdate() {
        updateUI()
    }

    fun onClickAdd() {
        if(!isEditMode){
            findNavController().navigate(R.id.action_favoriteListFragment_to_favoriteMapFragment)
            return
        }
        updateUI()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI(isInit: Boolean = false) {
        if(isEditMode || isInit){
            isEditMode = false
            adapter.isEditMode = false
            adapter.notifyDataSetChanged()
            binding.tvEdit.visibility = View.VISIBLE
            binding.btnAddOrOk.text = "추가하기"
        }else{
            isEditMode = true
            adapter.isEditMode = true
            adapter.notifyDataSetChanged()
            binding.tvEdit.visibility = View.GONE
            binding.btnAddOrOk.text = "확인"
        }
    }

    fun onClickBack() {
        findNavController().navigateUp()
    }

    override fun onResume() {
        super.onResume()
        updateUI(true)
    }


}