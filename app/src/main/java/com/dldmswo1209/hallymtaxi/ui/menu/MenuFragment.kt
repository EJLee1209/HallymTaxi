package com.dldmswo1209.hallymtaxi.ui.menu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.common.registerBackPressedFinishActivityCallback
import com.dldmswo1209.hallymtaxi.common.requestUpdate
import com.dldmswo1209.hallymtaxi.databinding.FragmentMenuBinding
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import com.dldmswo1209.hallymtaxi.util.PRIVACY_POLICY_URL
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment: Fragment() {
    private lateinit var binding: FragmentMenuBinding
    private var user: User? = null
    private val viewModel: MainViewModel by viewModels()
    private lateinit var myApplication: MyApplication
    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObserver()
        registerBackPressedFinishActivityCallback()
    }
    fun init(){
        myApplication = requireActivity().application as MyApplication
        user = myApplication.getUser() ?: kotlin.run {
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }

        binding.user = user
        binding.fragment = this
    }
    private fun setObserver() {
        viewModel.logout.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    val failToLogoutDialog = CustomDialog(
                        title = state.error ?: "로그아웃 실패",
                        content = "네트워크 상태를 확인해주세요",
                    )
                    failToLogoutDialog.show(parentFragmentManager, failToLogoutDialog.tag)
                }
                is UiState.Success -> {
                    loadingDialog.dismiss()
                    startActivity(Intent(requireActivity(), SplashActivity::class.java))
                    requireActivity().finish()
                }
            }
        }

        viewModel.inAppUpdate.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    when(state.error) {
                        "업데이트 없음" -> {
                            binding.tvVersion.text = "최신 버전 입니다."
                        }
                        else -> {
                            val failToCheckUpdate = CustomDialog(
                                title = state.error ?: "알 수 없는 오류",
                                content = "네트워크 상태를 확인해주세요",
                            )
                            failToCheckUpdate.show(parentFragmentManager, failToCheckUpdate.tag)
                        }
                    }
                }
                is UiState.Success -> {
                    loadingDialog.dismiss()
                    requireActivity().requestUpdate(state.data)
                }
            }
        }
    }


    fun onClickLogout(){
        user?.let {
            viewModel.logout()
        }
        val sharedPreferences = requireActivity().getSharedPreferences("loggedInfo", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("email", null)
            putString("password", null)
            apply()
        }
    }

    fun onClickFavorite(){
        findNavController().navigate(R.id.action_navigation_menu_to_favoriteListFragment)
    }

    fun onClickUserInfo(){
        findNavController().navigate(R.id.action_navigation_menu_to_userInfoFragment)
    }

    fun onClickUpdateInfo() {
        viewModel.checkAppUpdate()
    }

    fun onClickPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
        startActivity(intent)
    }
}