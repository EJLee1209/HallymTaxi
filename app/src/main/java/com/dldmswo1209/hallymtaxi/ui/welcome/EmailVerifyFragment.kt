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
import com.dldmswo1209.hallymtaxi.databinding.FragmentEmailVerifyBinding
import com.dldmswo1209.hallymtaxi.model.STATUS_OK
import com.dldmswo1209.hallymtaxi.vm.WelcomeViewModel

class EmailVerifyFragment: Fragment() {
    private lateinit var binding: FragmentEmailVerifyBinding
    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireContext())
    }
    private val viewModel : WelcomeViewModel by viewModels { ViewModelFactory(application = requireActivity().application) }
    private var email: String = ""
    private var isSentPrevious = false // 다음 화면으로 넘어갔다가 다시 돌아온 경우를 거르기 위한 플래그

    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{ // 키보드가 상태(true/false)
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
        binding = FragmentEmailVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        binding.etEmail.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.btnVerify.isEnabled = text.toString().isNotBlank() // 버튼 활성/비활성
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        keyboardInit()
        setObserver()
    }

    private fun keyboardInit(){
        KeyboardUtils.addKeyboardToggleListener((activity as WelcomeActivity), keyboardStateListener)
        binding.etEmail.setFocusAndShowKeyboard(requireContext())
    }

    private fun setObserver(){
        viewModel.existUser.observe(viewLifecycleOwner){
            if(it){ // 계정이 이미 존재
                val dialog = CustomDialog(
                    title = "재학생 인증",
                    content = "이미 계정이 존재합니다"
                ){
                    // 초기화면으로 이동
                    clickBackBtn()
                }
                dialog.isCancelable = false
                dialog.show(parentFragmentManager, dialog.tag)
            }
        }

        viewModel.isSent.observe(viewLifecycleOwner){
            if(it.status == STATUS_OK && !isSentPrevious){ // 인증메일 발송 완료
                val action =
                    EmailVerifyFragmentDirections.actionNavigationEmailVerifyToNavigationEmailVerifyCode(email)
                findNavController().navigate(action)
            }
            isSentPrevious = true
            loadingDialog.dismiss()
        }
    }

    fun clickVerifyBtn(){
        binding.etEmail.clearFocusAndHideKeyboard(requireContext())

        if(!getNetworkAvailable()){
            CustomDialog.checkNetworkDialog(parentFragmentManager)
            return
        }
        email = "${binding.etEmail.text}@hallym.ac.kr"
        viewModel.sendVerifyMail(email)
        loadingDialog.show()

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
        isSentPrevious = false
    }

}