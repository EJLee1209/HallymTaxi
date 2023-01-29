package com.dldmswo1209.hallymtaxi.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.databinding.FragmentWelcomeBinding

class WelcomeFragment: Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragment = this

    }

    fun clickLoginBtn(){
        findNavController().navigate(R.id.action_navigation_welcome_to_loginFragment)
    }
    fun clickRegisterBtn(){
        findNavController().navigate(R.id.action_navigation_welcome_to_navigation_email_verify)
    }
}