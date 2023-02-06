package com.dldmswo1209.hallymtaxi.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dldmswo1209.hallymtaxi.common.GlobalVariable
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.databinding.FragmentMenuBinding
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.vm.MainViewModel

class MenuFragment: Fragment() {
    private lateinit var binding: FragmentMenuBinding
    private var user: User? = null
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private lateinit var globalVariable: GlobalVariable
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init(){
        globalVariable = requireActivity().application as GlobalVariable
        user = globalVariable.getUser() ?: kotlin.run {
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }

        binding.user = user
        binding.fragment = this
    }

    fun onClickLogout(){
        user?.let { viewModel.logout(requireActivity(), it.uid) }
    }

}