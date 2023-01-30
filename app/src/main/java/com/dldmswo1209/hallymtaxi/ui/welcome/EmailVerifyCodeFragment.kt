package com.dldmswo1209.hallymtaxi.ui.welcome

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentEmailVerifyCodeBinding
import com.dldmswo1209.hallymtaxi.model.STATUS_FAIL
import com.dldmswo1209.hallymtaxi.model.STATUS_OK
import com.dldmswo1209.hallymtaxi.ui.compose.VerifyCodeTextField
import com.dldmswo1209.hallymtaxi.vm.WelcomeViewModel
import java.util.Timer
import kotlin.concurrent.timer

class EmailVerifyCodeFragment: Fragment() {
    private lateinit var binding: FragmentEmailVerifyCodeBinding
    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireContext())
    }
    private val viewModel : WelcomeViewModel by viewModels { ViewModelFactory(application = requireActivity().application) }
    private var email = ""
    private var copyCode = ""
    private var codeEffectiveTimer: Timer? = null

    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            viewMarginDynamicChanger.apply {
                val tvRegisterTitleOriginalTopMarginValue = MetricsUtil.convertDpToPixel(86, requireContext())
                val tvRegisterTitleSmallTopMarginValue = MetricsUtil.convertDpToPixel(5, requireContext())
                val btnVerifyOriginalBottomMarginValue = MetricsUtil.convertDpToPixel(47, requireContext())
                val btnVerifySmallBottomMarginValue = MetricsUtil.convertDpToPixel(5, requireContext())

                // 키보드 상태에따라 margin 동적 변경
                changeConstraintMarginTopBottom(binding.btnVerify,btnVerifyOriginalBottomMarginValue,btnVerifySmallBottomMarginValue,0,0,isVisible)
                changeConstraintMarginTopBottom(binding.tvRegisterTitle,0,0,tvRegisterTitleOriginalTopMarginValue,tvRegisterTitleSmallTopMarginValue, isVisible)
            }
        }
    }
    private val loadingDialog by lazy{
        LoadingDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmailVerifyCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        getArgsFromPreviousDestination()
        keyboardInit()
        setObserver()
        setComposeView()
        setTimer()

    }
    private fun getArgsFromPreviousDestination(){
        val args : EmailVerifyCodeFragmentArgs by navArgs()
        email = args.email
    }

    private fun keyboardInit(){
        KeyboardUtils.addKeyboardToggleListener((activity as WelcomeActivity), keyboardStateListener)
    }

    private fun setObserver(){
        viewModel.isVerified.observe(viewLifecycleOwner){
            loadingDialog.dismiss()
            when(it.status){
                STATUS_OK->{
                    if(it.message == "인증이 완료되었습니다."){
                        val action = EmailVerifyCodeFragmentDirections.actionNavigationEmailVerifyCodeToNavigationRegister(email)
                        findNavController().navigate(action)
                    }else{
                        // 인증 코드 불일치
                        binding.tvErrorCode.visibility = View.VISIBLE
                    }
                }
                STATUS_FAIL->{
                    // 인증 실패
                    binding.tvErrorCode.text = it.message
                    binding.tvErrorCode.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setComposeView(){
        // 코드 입력 UI 부분 compose 적용
        binding.composeViewVerifyCodeArea.setContent {
            // compose
            var code by remember { mutableStateOf("") }
            copyCode = code
            if(code.length == 8){
                binding.btnVerify.isEnabled = true
            }

            VerifyCodeTextField(code){
                if(it.length <= 8) code = it
            }
        }
    }

    private fun setTimer(){
        codeEffectiveTimer = timer(period = 1000){
            viewModel.continueTimer()
            val time = viewModel.codeEffectiveTime
            val hour = time/60
            val min = time%60
            val realTime = "$hour:%02d 안에 코드를 인증하세요".format(min)
            requireActivity().runOnUiThread{
                binding.tvCountDown.text = realTime
                if(time == 0){
                    binding.tvSendAgain.visibility = View.VISIBLE
                }
            }
        }
    }

    fun clickVerifyBtn(){
        if(!getNetworkAvailable()){
            CustomDialog.checkNetworkDialog(parentFragmentManager)
            return
        }
        viewModel.requestVerify(email, copyCode)
        loadingDialog.show()
    }

    fun clickBackBtn(){
        findNavController().popBackStack()
    }

    fun clickSendAgainBtn(){
        if(!getNetworkAvailable()){
            CustomDialog.checkNetworkDialog(parentFragmentManager)
            return
        }
        viewModel.sendVerifyMail(email)
        binding.tvSendAgain.visibility = View.GONE
        viewModel.resetTimer()
    }

    private fun getNetworkAvailable() : Boolean = (activity as WelcomeActivity).isNetworkActivate

    override fun onDetach() {
        super.onDetach()
        codeEffectiveTimer?.cancel()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }
}