package com.dldmswo1209.hallymtaxi.ui.welcome

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.common.keyboard.KeyboardUtils
import com.dldmswo1209.hallymtaxi.databinding.FragmentEmailVerifyBinding
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.util.AuthResponse
import com.dldmswo1209.hallymtaxi.data.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailVerifyFragment: Fragment() {
    private lateinit var binding: FragmentEmailVerifyBinding
    private val viewModel : AuthViewModel by viewModels()
    private var email: String = ""
    private var loadingDialog : LoadingDialog? = null
    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireActivity())
    }
    private var isFirst = true

    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{ // 키보드가 상태(true/false)
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            viewMarginDynamicChanger.apply {
                // 키보드 상태에따라 margin 동적 변경
                changeConstraintMarginTopBottom(binding.btnVerify,47.dp,5.dp,0,0,isVisible)
                changeConstraintMarginTopBottom(binding.tvRegisterTitle,0,0,86.dp,5.dp, isVisible)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmailVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        loadingDialog = LoadingDialog(requireActivity())
        keyboardInit()
        setObserver()
        etTextWatcher()
    }



    private fun keyboardInit(){
        KeyboardUtils.addKeyboardToggleListener((activity as WelcomeActivity), keyboardStateListener)
        binding.etEmail.setFocusAndShowKeyboard(requireActivity())
    }

    private fun setObserver(){
        viewModel.checkEmail.observe(viewLifecycleOwner){ state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog?.show()
                }
                is UiState.Failure -> {
                    loadingDialog?.dismiss()
                    val dialog = CustomDialog(
                        title = "재학생 인증",
                        content = state.error ?: AuthResponse.EMAIL_EXIST
                    ){
                        // 초기화면으로 이동
                        clickBackBtn()
                    }
                    dialog.isCancelable = false
                    dialog.show(parentFragmentManager, dialog.tag)
                }
                is UiState.Success ->{
                    loadingDialog?.dismiss()
                    viewModel.sendVerifyMail(email)
                }
            }
        }

        viewModel.isSentMail.observe(viewLifecycleOwner){state->
            if(!isFirst) return@observe
            when(state){
                is UiState.Loading -> {
                    loadingDialog?.show()
                }
                is UiState.Failure -> {
                    loadingDialog?.dismiss()
                    binding.tvGuide.text = state.error
                }
                is UiState.Success ->{
                    loadingDialog?.dismiss()
                    val action =
                        EmailVerifyFragmentDirections.actionNavigationEmailVerifyToNavigationEmailVerifyCode(email)
                    findNavController().navigate(action)
                    isFirst = false
                }
            }
        }
    }
    private fun etTextWatcher() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnVerify.isEnabled = text.toString().isNotBlank() // 버튼 활성/비활성
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    fun clickVerifyBtn(){
        binding.etEmail.clearFocusAndHideKeyboard(requireContext())

        if(!getNetworkAvailable()){
            CustomDialog.checkNetworkDialog(parentFragmentManager)
            return
        }
        email = "${binding.etEmail.text}@hallym.ac.kr"
        viewModel.checkEmail(email)
        isFirst = true
    }

    fun clickBackBtn(){
        binding.etEmail.clearFocusAndHideKeyboard(requireContext())
        findNavController().popBackStack()
    }

    private fun getNetworkAvailable() : Boolean = (activity as WelcomeActivity).isNetworkActivate

    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }

    override fun onResume() {
        super.onResume()
    }

}