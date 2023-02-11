package com.dldmswo1209.hallymtaxi.ui.carpool

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.paging.map
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentPoolListBottomSheetBinding
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.Collections

class PoolListBottomSheetFragment(
    private val onCreateRoomBtnClick: () -> Unit,
    private val joinRoomCallback: (CarPoolRoom) -> Unit,
    private val endPlace: Place,
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentPoolListBottomSheetBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }

    private var joinedRoom: CarPoolRoom? = null // 참여 중인 방
    private var room: CarPoolRoom? = null // 참여하려는 방
    private lateinit var user: User
    private lateinit var globalVariable: GlobalVariable

    private val loadingDialog by lazy {
        LoadingDialog(requireContext())
    }
    private lateinit var poolListAdapter: PoolListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetBehaviorSetting.bottomSheetBehaviorSetting(requireContext(), theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPoolListBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setObserver()
        pagingDataLoadFlow()
    }

    private fun init() {
        binding.fragment = this
        globalVariable = requireActivity().application as GlobalVariable

        user = globalVariable.getUser() ?: kotlin.run {
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }
        poolListAdapter = PoolListAdapter(
            requireActivity(),
            this@PoolListBottomSheetFragment,
            endPlace
        ) { room -> recyclerItemClickEvent(room) }
        binding.rvPool.adapter = poolListAdapter
    }

    private fun setObserver() {
        globalVariable.myRoom.observe(viewLifecycleOwner) { room ->
            joinedRoom = room
            poolListAdapter.roomId = room?.roomId
        }

        lifecycleScope.launch {
            viewModel.detachRoomPaging(user.gender).collectLatest {
                poolListAdapter.submitData(it) // submitData는 무효화 또는 새로고침 전까지 반환되지 않는 정지 함수
                // 여기부터 실행이 중단됨.
                loadingDialog.dismiss() // 새로고침이 완료 되면, 로딩 다이얼로그 숨김
            }
        }

        viewModel.isJoined.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.Default) {
                    loadingDialog.dismiss()
                    delay(100)
                }
                if (it) { // 입장 성공
                    room?.let { room ->
                        joinRoomCallback(room)
                    }

                } else { // 입장 실패(다이얼로그로 표시)
                    val dialog = CustomDialog(
                        title = "채팅방 입장 실패",
                        content = "채팅방 인원 초과 혹은\n마감된 채팅방 입니다"
                    ) {}
                    if(poolListAdapter.itemCount == 1){
                        this@PoolListBottomSheetFragment.dialog?.dismiss()
                    }
                    dialog.show(parentFragmentManager, dialog.tag)
                    poolListAdapter.refresh()
                }
            }
        }

    }

    private fun pagingDataLoadFlow() {
        lifecycleScope.launch {
            poolListAdapter.loadStateFlow.collectLatest {
                if (it.append is LoadState.Loading) loadingDialog.show()
                else loadingDialog.dismiss()
            }
        }
    }

    private fun recyclerItemClickEvent(room: CarPoolRoom) {
        // 채팅방 입장
        this.room = room
        joinedRoom?.let {
            if (it.roomId != room.roomId) {
                // 이미 참여중인 채팅방이 존재함
                val dialog = CustomDialog(
                    title = "채팅방 입장",
                    content = "이미 참여 중인\n채팅방이 존재합니다"
                ) {}
                dialog.show(parentFragmentManager, dialog.tag)
                return
            }
        }

        viewModel.joinRoom(room, user)
        loadingDialog.show()
    }

    fun onClickCreateRoom() {
        joinedRoom?.let {
            // 현재 참여하고 있는 방이 있음
            val dialog = CustomDialog(
                title = "방 만들기",
                content = "다른 채팅방을 생성하려면,\n참여하고 있는 방을 나가주세요"
            ) {}
            dialog.show(parentFragmentManager, dialog.tag)

            return
        }

        onCreateRoomBtnClick()
        dialog?.dismiss()
    }

    fun onClickRefreshRoomList() {
        poolListAdapter.refresh()
        loadingDialog.show()
    }

    fun visibilityNoPoolRoomLayout(isVisible: Boolean) {
        if (isVisible) binding.layoutNoPoolRoom.visibility = View.VISIBLE
        else binding.layoutNoPoolRoom.visibility = View.GONE
    }
}