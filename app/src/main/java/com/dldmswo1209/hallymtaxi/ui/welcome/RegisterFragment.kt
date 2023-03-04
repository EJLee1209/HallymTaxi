package com.dldmswo1209.hallymtaxi.ui.welcome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.common.keyboard.KeyboardUtils
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog.Companion.checkNetworkDialog
import com.dldmswo1209.hallymtaxi.databinding.FragmentRegisterBinding
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.ui.welcome.compose.RegisterScreen
import com.dldmswo1209.hallymtaxi.util.PRIVACY_POLICY_URL
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment: Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private var email = ""

    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireActivity())
    }
    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{ // 키보드가 상태(true/false)
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            viewMarginDynamicChanger.apply {
                changeConstraintMarginTopBottom(binding.tvRegisterTitle,0,0,86.dp,5.dp, isVisible)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        registerBackPressedCallback(R.id.action_navigation_register_to_navigation_welcome)
        getArgsFromPreviousDestination() // 이전 fragment로부터 데이터 가져오기
        keyboardInit() // 키보드 상태 리스너 세팅
        composeViewSetContent() // composeView setContent
    }

    private fun getArgsFromPreviousDestination(){
        val args : EmailVerifyCodeFragmentArgs by navArgs()
        email = args.email
    }

    private fun keyboardInit(){
        KeyboardUtils.addKeyboardToggleListener((activity as WelcomeActivity), keyboardStateListener)
    }

    private fun composeViewSetContent(){
        binding.composeViewRegisterArea.setContent {
            RegisterScreen(email = email, onClickPrivacyPolicyViewContent = onClickPrivacyPolicyViewContent) {
                // 회원가입 완료시 초기 화면으로 이동
                clickBackBtn()
            }
        }
    }
    private val onClickPrivacyPolicyViewContent:() -> Unit = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
        startActivity(intent)
    }

    fun clickBackBtn(){
        // 백버튼 클릭시 초기 화면으로 돌아감
        findNavController().navigate(R.id.action_navigation_register_to_navigation_welcome)
    }

    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }
}