package com.dldmswo1209.hallymtaxi.ui.carpool

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentPoolListBottomSheetBinding
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import java.util.Collections

class PoolListBottomSheetFragment(
    private val onCreateRoomBtnClick: () -> Unit,
    private val joinRoomCallback: (CarPoolRoom) -> Unit,
    private val endPlace: Place
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentPoolListBottomSheetBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private var joinedRoom: CarPoolRoom? = null
    private var room: CarPoolRoom? = null
    private lateinit var user: User
    private val loadingDialog by lazy{
        LoadingDialog(requireContext())
    }

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

    }

    private fun init() {
        binding.fragment = this
        user = (activity as MainActivity).detachUserInfo() ?: kotlin.run {
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }
    }

    private fun setObserver() {
        viewModel.poolList.observe(viewLifecycleOwner) { roomList ->
            val filteredRoomList = filterRoom(roomList)
            checkMyRoom(filteredRoomList)
            setRecyclerViewAdapter(filteredRoomList)

            if (filteredRoomList.isEmpty()) binding.tvNoPoolRoom.visibility = View.VISIBLE
            else binding.tvNoPoolRoom.visibility = View.INVISIBLE
            loadingDialog.dismiss()
        }

        viewModel.isJoined.observe(viewLifecycleOwner){
            CoroutineScope(Dispatchers.Main).launch{
                withContext(Dispatchers.Default) {
                    loadingDialog.dismiss()
                    delay(100)
                }
                if(it){ // 입장 성공
                    room?.let{ room->
                        joinRoomCallback(room)
                    }

                }else{ // 입장 실패(다이얼로그로 표시)
                    val dialog = CustomDialog(
                        title = "채팅방 입장 실패",
                        content = "채팅방 인원 초과 혹은\n삭제된 채팅방 입니다"
                    ) {}
                    dialog.show(parentFragmentManager, dialog.tag)
                    viewModel.detachAllRoom() // 채팅방 새로고침
                }
            }
        }

        viewModel.detachAllRoom()
    }

    private fun checkMyRoom(roomList: List<CarPoolRoom>){
        roomList.forEach { room->
            if(room.participants.contains(user)){
                // 내가 속한 방이 존재
                joinedRoom = room
            }
        }
    }

    private fun filterRoom(roomList: List<CarPoolRoom>) : List<CarPoolRoom>{
        val filteredRoomList = roomList.toMutableList()

        for(idx in filteredRoomList.size-1 downTo 0){
            if(filteredRoomList[idx].genderOption != GENDER_OPTION_NONE && filteredRoomList[idx].genderOption != user.gender){
                filteredRoomList.remove(filteredRoomList[idx]) // 성별 조건에 부합 하지 않은 방을 필터링
            }
        }

        return filteredRoomList
    }

    private fun setRecyclerViewAdapter(roomList: List<CarPoolRoom>){
        val sortedRoomList = getSortedListByDistance(roomList)
        val distanceList = getDistanceList(sortedRoomList)
        binding.rvPool.adapter = PoolListAdapter(joinedRoom, requireContext(), distanceList) { room ->
            recyclerItemClickEvent(room)
        }.apply {
            submitList(sortedRoomList)
        }
    }

    private fun getDistanceList(roomList: List<CarPoolRoom>) : List<Int>{
        val distanceList = mutableListOf<Int>()
        roomList.forEach { room->  // 모든 카풀방 거리 계산
            distanceList.add(DistanceManager.getDistance(room.endPlace.y, room.endPlace.x, endPlace.y, endPlace.x))
        }

        return distanceList
    }

    private fun recyclerItemClickEvent(room: CarPoolRoom){
        // 채팅방 입장
        this.room = room
        joinedRoom?.let{
            if(it.roomId != room.roomId) {
                // 이미 참여중인 채팅방이 존재함
                val dialog = CustomDialog(
                    title = "채팅방 입장",
                    content = "이미 참여 중인\n채팅방이 존재합니다"
                ) {}
                dialog.show(parentFragmentManager, dialog.tag)
                return
            }
        }

        viewModel.joinRoom(room,user)
        loadingDialog.show()
    }
    private fun getSortedListByDistance(roomList: List<CarPoolRoom>) : List<CarPoolRoom>{
        // 현재 내 목적지와 거리를 비교해서 정렬
        val sortedList = roomList.sortedWith(compareBy { DistanceManager.getDistance(it.endPlace.y, it.endPlace.x, endPlace.y, endPlace.x) }).toMutableList()

        joinedRoom?.let{
            // 현재 참여중인 방을 맨 앞으로
            sortedList.remove(it)
            sortedList.add(0, it)
        }

        return sortedList
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
        viewModel.detachAllRoom()
        loadingDialog.show()
    }
}