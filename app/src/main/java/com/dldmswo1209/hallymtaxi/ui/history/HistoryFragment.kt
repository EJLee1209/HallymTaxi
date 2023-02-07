package com.dldmswo1209.hallymtaxi.ui.history

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.dldmswo1209.hallymtaxi.common.GlobalVariable
import com.dldmswo1209.hallymtaxi.common.TimeService
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.databinding.FragmentHistoryBinding
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.model.RoomInfo
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.google.api.ResourceDescriptor

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private lateinit var user: User
    private var joinedRoom: CarPoolRoom? = null
    private var history: List<RoomInfo> = listOf()
    private lateinit var globalVariable: GlobalVariable
    private lateinit var historyListAdapter: HistoryListAdapter

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
        registerBroadcastReceiver()
    }

    private fun init() {
        globalVariable = requireActivity().application as GlobalVariable
        user = globalVariable.getUser() ?: kotlin.run {
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }
        binding.fragment = this
        viewModel.detachRoomInfoHistory()

        historyListAdapter = HistoryListAdapter {
            val action = HistoryFragmentDirections.actionNavigationHistoryToChatRoomHistoryFragment(it.roomId)
            findNavController().navigate(action)
        }
        binding.rvHistory.adapter = historyListAdapter

    }

    private fun setObservers() {
        globalVariable.myRoom.observe(viewLifecycleOwner){
            Log.d("testt", "myRoom: ${it}")
            if(it == null) {
                if(history.isEmpty()) binding.layoutNoPoolRoom.visibility = View.VISIBLE
                binding.layoutCurrentJoinedRoom.visibility = View.GONE
                return@observe
            }
            binding.room = it
            joinedRoom = it
            viewModel.detachRoomInfo(it.roomId)
            binding.layoutCurrentJoinedRoom.visibility = View.VISIBLE
            binding.layoutNoPoolRoom.visibility = View.GONE
        }

        viewModel.roomInfo.observe(viewLifecycleOwner){
            binding.roomInfo = it
        }
        viewModel.roomHistory.observe(viewLifecycleOwner){
            Log.d("testt", "room history: ${it}")
            if(it.isEmpty()){
                if(joinedRoom == null) binding.layoutNoPoolRoom.visibility = View.VISIBLE
                return@observe
            }
            binding.layoutNoPoolRoom.visibility = View.GONE
            history = sortedWithDate(it)
            historyListAdapter.submitList(history)
        }
        
    }

    private fun registerBroadcastReceiver() {
        // 새로운 메세지가 온 경우 브로드 캐스트 리시버를 통해 알 수 있음
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    joinedRoom?.let { viewModel.detachRoomInfo(it.roomId) }// 채팅 내역 조회
                }

            }, IntentFilter("newMessage"))
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

}