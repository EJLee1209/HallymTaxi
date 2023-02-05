package com.dldmswo1209.hallymtaxi.ui.history

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.common.TimeService
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.databinding.FragmentHistoryBinding
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.model.RoomInfo
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.google.api.ResourceDescriptor

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private lateinit var user: User
    private var joinedRoom: CarPoolRoom? = null
    private var lastChatKey: Int = -1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setObservers()
    }

    private fun init() {
        user = (activity as MainActivity).detachUserInfo()
        joinedRoom = (activity as MainActivity).joinedRoom
        binding.fragment = this
        val sharedPreference = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        lastChatKey = sharedPreference.getInt("lastChatKey", -1)
    }

    private fun setObservers() {
        joinedRoom?.let {
            viewModel.detachRoomInfo(it.roomId).observe(viewLifecycleOwner) { room ->
                binding.roomInfo = room
                binding.layoutCurrentJoinedRoom.visibility = View.VISIBLE
                if (room.lastChatKey != lastChatKey) { // 새로운 메세지가 옴
                    binding.ivNewChat.visibility = View.VISIBLE
                } else {
                    binding.ivNewChat.visibility = View.GONE
                }
            }
        }

//        HistoryListAdapter { roomInfo ->
//            // 히스토리 클릭 이벤트
//            val action =
//                HistoryFragmentDirections.actionNavigationHistoryToChatRoomHistoryFragment(roomInfo)
//            findNavController().navigate(action)
//        }.apply {
//            viewModel.detachHistory().observe(viewLifecycleOwner) { poolList ->
//                binding.rvHistory.adapter = this
//                val sortedList = sortedWithDate(poolList)
//                submitList(sortedList)
//                if (sortedList.isEmpty() && joinedRoom == null) {
//                    binding.tvNoPoolRoom.visibility = View.VISIBLE
//                } else {
//                    binding.tvNoPoolRoom.visibility = View.GONE
//                }
//            }
//        }
    }
    private fun sortedWithDate(poolList: List<RoomInfo>) : List<RoomInfo>{
        return poolList.sortedWith(compareBy<RoomInfo> {
            TimeService.dateTimeSplitHelper(it.lastReceiveMsgDateTime, "year")}
            .thenBy { TimeService.dateTimeSplitHelper(it.lastReceiveMsgDateTime, "month") }
            .thenBy { TimeService.dateTimeSplitHelper(it.lastReceiveMsgDateTime, "day") }
            .thenBy { TimeService.dateTimeSplitHelper(it.lastReceiveMsgDateTime, "hour") }
            .thenBy { TimeService.dateTimeSplitHelper(it.lastReceiveMsgDateTime, "min") }
            .thenBy { TimeService.dateTimeSplitHelper(it.lastReceiveMsgDateTime, "sec") })
            .reversed()
    }

    fun onClickJoinedRoom() {
        // 현재 참여중인 채팅방 클릭 이벤트
        joinedRoom?.let {
            val action = HistoryFragmentDirections.actionNavigationHistoryToChatRoomFragment(it)
            findNavController().navigate(action)
        }
    }

    override fun onDetach() {
        super.onDetach()

        viewModel.allListenerRemove()
    }

    override fun onPause() {
        super.onPause()

        viewModel.allListenerRemove()
    }
}