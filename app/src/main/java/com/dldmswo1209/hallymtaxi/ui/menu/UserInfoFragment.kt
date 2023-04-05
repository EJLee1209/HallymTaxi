package com.dldmswo1209.hallymtaxi.ui.menu

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.common.registerBackPressedCallback
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentUserInfoBinding
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.ui.welcome.WelcomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class UserInfoFragment : Fragment() {
    private lateinit var binding: FragmentUserInfoBinding
    private lateinit var myApplication: MyApplication
    private var user: User? = null

    private val viewModel: MainViewModel by viewModels()
    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setObserver()
        registerBackPressedCallback()
    }

    private fun init() {
        binding.fragment = this
        myApplication = requireActivity().application as MyApplication
    }

    private fun setObserver(){
        myApplication.userLiveData.observe(viewLifecycleOwner) { user ->
            this.user = user
            binding.user = user
        }

        viewModel.deleteAccount.observe(viewLifecycleOwner) {
            when(it) {
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                }
                is UiState.Success -> {
                    loadingDialog.dismiss()
                    startActivity(Intent(requireContext(), WelcomeActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    fun onClickBack() {
        findNavController().navigateUp()
    }

    fun onClickDeleteAccount() {
        val deleteDialog = CustomDialog(
            title = "회원 탈퇴",
            content = "정말 계정을 삭제하시겠습니까?",
            positiveCallback = {
                viewModel.deleteAccount()
            },
            positiveButton = "확인",
            negativeButtonVisible = true,
            negativeButton = "취소",
        )
        deleteDialog.show(parentFragmentManager, deleteDialog.tag)

    }

}