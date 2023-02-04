package com.dldmswo1209.hallymtaxi.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.common.ViewModelFactory
import com.dldmswo1209.hallymtaxi.databinding.FragmentChatRoomHistoryBinding
import com.dldmswo1209.hallymtaxi.model.RoomInfo
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.ui.carpool.ChatListAdapter
import com.dldmswo1209.hallymtaxi.vm.MainViewModel

class ChatRoomHistoryFragment: Fragment() {
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private lateinit var binding: FragmentChatRoomHistoryBinding
    private lateinit var roomInfo: RoomInfo
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatRoomHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

    }

    private fun init(){
        val args : ChatRoomHistoryFragmentArgs by navArgs()
        roomInfo = args.roomInfo
        user = (activity as MainActivity).detachUserInfo()
        binding.fragment = this

        binding.tvRoomTitle.text = "${roomInfo.startPlace.place_name} - ${roomInfo.endPlace.place_name}"

//        ChatListAdapter(user,).apply {
//            viewModel.getHistoryMessage(roomInfo.roomId).observe(viewLifecycleOwner){
//                binding.rvMessage.adapter = this
//                submitList(it)
//            }
//        }
    }

    fun onClickBack(){
        findNavController().popBackStack()
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.allListenerRemove()
    }
}