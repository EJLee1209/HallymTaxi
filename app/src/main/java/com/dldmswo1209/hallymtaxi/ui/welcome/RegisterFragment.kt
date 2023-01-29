package com.dldmswo1209.hallymtaxi.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.KeyboardUtils
import com.dldmswo1209.hallymtaxi.common.MetricsUtil
import com.dldmswo1209.hallymtaxi.common.ViewMarginDynamicChanger
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.databinding.FragmentRegisterBinding
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.ui.compose.RegisterScreen
import com.dldmswo1209.hallymtaxi.vm.WelcomeViewModel

class RegisterFragment: Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private var email = ""
    private val viewModel : WelcomeViewModel by viewModels { ViewModelFactory(application = requireActivity().application) }

    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireContext())
    }
    private lateinit var callback: OnBackPressedCallback

    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{ // 키보드가 상태(true/false)
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            viewMarginDynamicChanger.apply {
                val tvRegisterTitleOriginalTopMarginValue = MetricsUtil.convertDpToPixel(86, requireContext())
                val tvRegisterTitleSmallTopMarginValue = MetricsUtil.convertDpToPixel(5, requireContext())

                changeConstraintMarginTopBottom(binding.tvRegisterTitle,0,0,tvRegisterTitleOriginalTopMarginValue,tvRegisterTitleSmallTopMarginValue, isVisible)
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

        getArgsFromPreviousDestination() // 이전 fragment로부터 데이터 가져오기
        keyboardInit() // 키보드 상태 리스너 세팅
        backPressedSetCallback() // back key 가 눌렸을 때 이벤트 처리
        composeViewSetContent() // composeView setContent

    }

    private fun getArgsFromPreviousDestination(){
        val args : EmailVerifyCodeFragmentArgs by navArgs()
        email = args.email
    }

    private fun keyboardInit(){
        KeyboardUtils.addKeyboardToggleListener((activity as WelcomeActivity), keyboardStateListener)
    }

    private fun backPressedSetCallback(){
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                clickBackBtn()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun composeViewSetContent(){
        binding.composeViewRegisterArea.setContent {

            val isCreated = viewModel.isCreatedUser.observeAsState(initial = false)
            val registerButtonClickCallback : (User, String)->(Unit) = { user, password->
                viewModel.createUser(user, password)
            }

            RegisterScreen(email = email, isCreated = isCreated.value, onClickRegister = registerButtonClickCallback) {
                // 회원가입 완료시 초기 화면으로 이동
                clickBackBtn()
            }
        }
    }

    fun clickBackBtn(){
        // 백버튼 클릭시 초기 화면으로 돌아감
        findNavController().navigate(R.id.action_navigation_register_to_navigation_welcome)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }
}