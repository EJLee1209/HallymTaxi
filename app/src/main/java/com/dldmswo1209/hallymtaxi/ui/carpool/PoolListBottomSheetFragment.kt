package com.dldmswo1209.hallymtaxi.ui.carpool

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dldmswo1209.hallymtaxi.common.BottomSheetBehaviorSetting
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.common.location.DistanceManager
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentPoolListBottomSheetBinding
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.dldmswo1209.hallymtaxi.util.FireStoreResponse.JOIN_ROOM_SUCCESS
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import com.dldmswo1209.hallymtaxi.util.FireStoreResponse.JOIN_ROOM_ALREADY_JOINED
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class PoolListBottomSheetFragment(
    private val onCreateRoomBtnClick: () -> Unit,
    private val joinRoomCallback: (CarPoolRoom) -> Unit,
    private val endPlace: Place,
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentPoolListBottomSheetBinding
    private val viewModel: MainViewModel by viewModels()

    private var joinedRoom: CarPoolRoom? = null // 참여 중인 방
    private var room: CarPoolRoom? = null // 참여하려는 방
    private lateinit var user: User
    private lateinit var myApplication: MyApplication
    private var myToken : String = ""

    private val loadingDialog by lazy {
        LoadingDialog(requireActivity())
    }
    private lateinit var poolListAdapter: PoolListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetBehaviorSetting.bottomSheetBehaviorSetting(requireActivity(), theme)
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
    }

    private fun init() {
        binding.fragment = this
        myApplication = requireActivity().application as MyApplication

        user = myApplication.getUser() ?: kotlin.run {
            startActivity(Intent(requireActivity(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }
        myToken = myApplication.getFcmToken()

        poolListAdapter = PoolListAdapter(
            requireActivity(),
            this@PoolListBottomSheetFragment,
            endPlace
        ) { room -> recyclerItemClickEvent(room) }
        binding.rvPool.adapter = poolListAdapter
    }

    private fun setObserver() {
        myApplication.myRoom.observe(viewLifecycleOwner) { room ->
            joinedRoom = room
            poolListAdapter.roomId = room?.roomId
        }

        viewModel.carPoolList.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    poolListAdapter.submitList(orderedCarPoolList(state.data))
                }
            }
        }
        viewModel.getAllRoom(user.gender)

        viewModel.getParticipantsTokens.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                }
                is UiState.Success ->{
                    val tokens = state.data.toMutableList()
                    tokens.remove(myToken)

                    room?.let { room ->
                        val chat = Chat(
                            roomId = room.roomId,
                            userId = user.uid,
                            userName = user.name,
                            msg = "${user.name}님이 입장하셨습니다",
                            messageType = CHAT_JOIN
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.sendMessage(chat = chat, userName = user.name, receiveTokens = tokens).join()
                            withContext(Dispatchers.Main){
                                loadingDialog.dismiss()
                                joinRoomCallback(room)
                            }
                        }
                    }

                }
            }
        }

        viewModel.joinRoom.observe(viewLifecycleOwner) { state->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {
                    loadingDialog.dismiss()
                    val dialog = CustomDialog(
                        title = "채팅방 입장 실패",
                        content = state.error ?: "채팅방 인원 초과 혹은\n마감된 채팅방 입니다"
                    ) {}
                    dialog.show(parentFragmentManager, dialog.tag)
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()

                    room?.let { room ->
                        when(state.data) {
                            JOIN_ROOM_SUCCESS -> {
                                viewModel.getParticipantsTokens(room.roomId)
                            }
                            JOIN_ROOM_ALREADY_JOINED -> {
                                joinRoomCallback(room)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun orderedCarPoolList(poolList: List<CarPoolRoom>): MutableList<CarPoolRoom> {
        // 유저가 설정한 목적지 기준 오름차순 정렬
        return poolList.sortedWith(compareBy {
            DistanceManager.getDistance(endPlace.y, endPlace.x, it.endPlace.y, it.endPlace.x)
        }).toMutableList()
    }

    private fun recyclerItemClickEvent(room: CarPoolRoom) {
        // 채팅방 입장
        this.room = room
        joinedRoom?.let {
            if (it.roomId.isNotBlank()) {
                // 이미 참여중인 채팅방이 존재함
                if(it == room) {
                    viewModel.joinRoom(room)
                }else{
                    val dialog = CustomDialog(
                        title = "채팅방 입장",
                        content = "이미 참여 중인\n채팅방이 존재합니다"
                    ) {}
                    dialog.show(parentFragmentManager, dialog.tag)
                    return
                }
            }
        }
        viewModel.joinRoom(room)
    }

    fun onClickCreateRoom() {
        joinedRoom?.let {
            // 현재 참여하고 있는 방이 있음
            if(it.roomId.isNotBlank()) {
                val dialog = CustomDialog(
                    title = "방 만들기",
                    content = "다른 채팅방을 생성하려면,\n참여하고 있는 방을 나가주세요"
                ) {}
                dialog.show(parentFragmentManager, dialog.tag)

                return
            }
        }

        onCreateRoomBtnClick()
        dialog?.dismiss()
    }

    fun onClickRefreshRoomList() {
        viewModel.getAllRoom(user.gender)
        loadingDialog.show()
    }

    fun visibilityNoPoolRoomLayout(isVisible: Boolean) {
        if (isVisible) binding.layoutNoPoolRoom.visibility = View.VISIBLE
        else binding.layoutNoPoolRoom.visibility = View.GONE
    }
}