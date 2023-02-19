package com.dldmswo1209.hallymtaxi.ui.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide.init
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.databinding.FragmentFavoriteListBinding

class FavoriteListFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteListBinding

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
    }

    private fun init(){
        binding.fragment = this

    }

    fun onClickAdd(){
        findNavController().navigate(R.id.action_favoriteListFragment_to_favoriteMapFragment)
    }

    fun onClickBack(){
        findNavController().popBackStack()
    }
}