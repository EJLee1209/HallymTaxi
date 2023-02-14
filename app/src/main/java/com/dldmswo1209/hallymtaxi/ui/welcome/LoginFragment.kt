package com.dldmswo1209.hallymtaxi.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.common.keyboard.KeyboardUtils
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog.Companion.checkNetworkDialog
import com.dldmswo1209.hallymtaxi.databinding.FragmentLoginBinding
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.util.UiState
import com.dldmswo1209.hallymtaxi.vm.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment: Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel : AuthViewModel by viewModels()
    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
    }
    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireActivity())
    }
    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            val tvLoginTitleOriginalTopMarginValue = MetricsUtil.convertDpToPixel(86, requireActivity())
            val tvLoginTitleSmallTopMarginValue = MetricsUtil.convertDpToPixel(5, requireActivity())
            val btnLoginOriginalBottomMarginValue = MetricsUtil.convertDpToPixel(47, requireActivity())
            val btnLoginSmallBottomMarginValue = MetricsUtil.convertDpToPixel(5, requireActivity())

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

        viewModel.login.observe(viewLifecycleOwner){state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    // 로그인 실패
                    loadingDialog.dismiss()
                    binding.tvErrorMessage.visibility = View.VISIBLE
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    // 로그인 성공
                    startActivity(Intent(requireActivity(), SplashActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    fun clickLoginBtn(){
        binding.etEmail.clearFocusAndHideKeyboard(requireActivity())
        binding.etPassword.clearFocusAndHideKeyboard(requireActivity())

        if(!getNetworkAvailable()){
            checkNetworkDialog(parentFragmentManager)
            return
        }
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        viewModel.login(email, password)
    }

    fun clickBackBtn(){
        findNavController().popBackStack()
        binding.etEmail.clearFocusAndHideKeyboard(requireActivity())
        binding.etPassword.clearFocusAndHideKeyboard(requireActivity())
    }

    private fun getNetworkAvailable() : Boolean = (activity as WelcomeActivity).isNetworkActivate


    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }
}