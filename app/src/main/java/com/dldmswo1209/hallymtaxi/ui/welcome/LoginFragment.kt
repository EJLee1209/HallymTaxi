package com.dldmswo1209.hallymtaxi.ui.welcome

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.common.keyboard.KeyboardUtils
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog.Companion.checkNetworkDialog
import com.dldmswo1209.hallymtaxi.databinding.FragmentLoginBinding
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.util.AuthResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment: Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private var email : String = ""
    private var password : String = ""
    private val viewModel : AuthViewModel by viewModels()
    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
    }
    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireActivity())
    }
    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            viewMarginDynamicChanger.changeConstraintMarginTopBottom(binding.tvLoginTitle,0,0,86.dp,5.dp, isVisible)
            viewMarginDynamicChanger.changeConstraintMarginTopBottom(binding.btnLogin,47.dp,5.dp,0,0,isVisible)
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

        registerBackPressedCallback()

        KeyboardUtils.addKeyboardToggleListener((activity as WelcomeActivity), keyboardStateListener)
        binding.etEmail.setFocusAndShowKeyboard(requireContext())

        viewModel.checkLogged.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    // 로그인 불가능 (이미 로그인 되어 있는 기기가 있음)
                    loadingDialog.dismiss()
                    if(state.error == AuthResponse.LOGIN_IMPOSSIBLE) {
                        val forceLoginDailog = CustomDialog(
                            "로그인",
                            content = "다른 기기에서 로그인 하셨군요!\n접송 중인 모든 기기의 연결을 끊고\n로그인 하시겠습니까?",
                            positiveButton = "로그인",
                            negativeButtonVisible = true,
                            positiveCallback = {
                                viewModel.login(email, password, requireActivity().getDeviceId())
                            }
                        )
                        forceLoginDailog.show(parentFragmentManager, forceLoginDailog.tag)
                    }
                    binding.tvErrorMessage.text = state.error
                    binding.tvErrorMessage.visibility = View.VISIBLE
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    // 로그인 가능
                    viewModel.login(email, password, requireActivity().getDeviceId())
                }
            }
        }

        viewModel.login.observe(viewLifecycleOwner){state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    // 로그인 실패
                    loadingDialog.dismiss()
                    binding.tvErrorMessage.text = state.error
                    binding.tvErrorMessage.visibility = View.VISIBLE
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    // 로그인 성공
                    savePreferencesLoggedInfo()
                    val intent = Intent(requireActivity(), SplashActivity::class.java)
                    intent.putExtra("fromLoginFragment", true)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }

        viewModel.sendPasswordResetMail.observe(viewLifecycleOwner) { state ->
            when(state) {
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    val sendFailureDialog = CustomDialog(
                        title = "비밀번호 재설정",
                        content = state.error ?: AuthResponse.SEND_PASSWORD_RESET_UNKNOWN_ERROR,
                    )
                    sendFailureDialog.show(parentFragmentManager, sendFailureDialog.tag)
                }
                is UiState.Success -> {
                    loadingDialog.dismiss()
                    val sendSuccessDialog = CustomDialog(
                        title = "비밀번호 재설정",
                        content = state.data,
                    )
                    sendSuccessDialog.show(parentFragmentManager, sendSuccessDialog.tag)
                }
            }

        }
    }

    private fun savePreferencesLoggedInfo() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("loggedInfo", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("email", email)
            .putString("password", password)
            .apply()
    }

    private fun clearFocusAll() {
        binding.etEmail.clearFocusAndHideKeyboard(requireActivity())
        binding.etPassword.clearFocusAndHideKeyboard(requireActivity())
    }

    fun clickLoginBtn(){
        clearFocusAll()

        email = binding.etEmail.text.toString()
        password = binding.etPassword.text.toString()

        viewModel.checkLogged(email, requireActivity().getDeviceId())
    }

    fun clickBackBtn(){
        findNavController().navigateUp()
        clearFocusAll()
    }

    fun onClickForgotPassword() {
        viewModel.sendPasswordResetMail(binding.etEmail.text.toString())
        clearFocusAll()
    }


    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }
}