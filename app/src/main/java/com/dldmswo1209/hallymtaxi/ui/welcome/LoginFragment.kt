package com.dldmswo1209.hallymtaxi.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.SplashActivity
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentLoginBinding
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.dldmswo1209.hallymtaxi.vm.WelcomeViewModel
import kotlinx.coroutines.*

class LoginFragment: Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireContext())
    }
    private val viewModel : WelcomeViewModel by viewModels { ViewModelFactory(application = requireActivity().application) }
    private val mainViewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }

    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            val tvLoginTitleOriginalTopMarginValue = MetricsUtil.convertDpToPixel(86, requireContext())
            val tvLoginTitleSmallTopMarginValue = MetricsUtil.convertDpToPixel(5, requireContext())
            val btnLoginOriginalBottomMarginValue = MetricsUtil.convertDpToPixel(47, requireContext())
            val btnLoginSmallBottomMarginValue = MetricsUtil.convertDpToPixel(5, requireContext())

            viewMarginDynamicChanger.changeConstraintMarginTopBottom(binding.tvLoginTitle,0,0,tvLoginTitleOriginalTopMarginValue,tvLoginTitleSmallTopMarginValue, isVisible)
            viewMarginDynamicChanger.changeConstraintMarginTopBottom(binding.btnLogin,btnLoginOriginalBottomMarginValue,btnLoginSmallBottomMarginValue,0,0,isVisible)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        KeyboardUtils.addKeyboardToggleListener((activity as WelcomeActivity), keyboardStateListener)
        binding.etEmail.setFocusAndShowKeyboard(requireContext())

        viewModel.loginResult.observe(viewLifecycleOwner){
            if(it){
                // 로그인 성공
                startActivity(Intent(requireContext(), SplashActivity::class.java))
                requireActivity().finish()
            }else{
                // 로그인 실패
                binding.tvErrorMessage.visibility = View.VISIBLE
            }
        }
    }

    fun clickLoginBtn(){
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        viewModel.login(email, password)
        binding.etEmail.clearFocusAndHideKeyboard(requireContext())
        binding.etPassword.clearFocusAndHideKeyboard(requireContext())
    }

    fun clickBackBtn(){
        findNavController().popBackStack()
        binding.etEmail.clearFocusAndHideKeyboard(requireContext())
        binding.etPassword.clearFocusAndHideKeyboard(requireContext())
    }

    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }
}