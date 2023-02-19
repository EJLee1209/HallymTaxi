package com.dldmswo1209.hallymtaxi.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.databinding.FragmentMenuBinding
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.viewmodel.MainViewModel
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
    }


    fun onClickLogout(){
        user?.let {
            viewModel.logout(it.uid)
        }
    }

    fun onClickFavorite(){
        findNavController().navigate(R.id.action_navigation_menu_to_favoriteListFragment)
    }
}