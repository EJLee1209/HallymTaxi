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
                    if(state.error == "다른 기기에서 이미 로그인 했습니다") {
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
    }

    private fun savePreferencesLoggedInfo() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("loggedInfo", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("email", email)
            .putString("password", password)
            .apply()
    }

    fun clickLoginBtn(){
        binding.etEmail.clearFocusAndHideKeyboard(requireActivity())
        binding.etPassword.clearFocusAndHideKeyboard(requireActivity())

        if(!getNetworkAvailable()){
            checkNetworkDialog(parentFragmentManager)
            return
        }
        email = binding.etEmail.text.toString()
        password = binding.etPassword.text.toString()

        viewModel.checkLogged(email, requireActivity().getDeviceId())
    }

    fun clickBackBtn(){
        findNavController().popBackStack()
        binding.etEmail.clearFocusAndHideKeyboard(requireActivity())
        binding.etPassword.clearFocusAndHideKeyboard(requireActivity())
    }

    fun onClickForgotPassword() {
        val forgotPasswordDialog = CustomDialog(
            title = "안내 메세지",
            content = "아직 개발 중인 기능 입니다\n연락 주시면 도와드리겠습니다",
            positiveButton = "연락 하기",
            positiveCallback = {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.kakao.com/o/s2SRy56e"))
                startActivity(intent)
            }
        )
        forgotPasswordDialog.show(parentFragmentManager, forgotPasswordDialog.tag)
    }

    private fun getNetworkAvailable() : Boolean = (activity as WelcomeActivity).isNetworkActivate


    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }
}