package com.dldmswo1209.hallymtaxi.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.common.MyApplication
import com.dldmswo1209.hallymtaxi.databinding.FragmentChatRoomHistoryBinding
import com.dldmswo1209.hallymtaxi.data.model.User
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.ui.carpool.ChatListAdapter
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatRoomHistoryFragment: Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentChatRoomHistoryBinding
    private lateinit var roomId: String
    private lateinit var myApplication: MyApplication
    private lateinit var user: User
    private lateinit var chatListAdapter: ChatListAdapter

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
        setObservers()
    }

    private fun init(){
        val args : ChatRoomHistoryFragmentArgs by navArgs()
        roomId = args.roomId
        myApplication = requireActivity().application as MyApplication
        user = myApplication.getUser() ?: kotlin.run {
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }
        binding.fragment = this
        chatListAdapter = ChatListAdapter(user)
        binding.rvMessage.adapter = chatListAdapter
    }

    private fun setObservers(){
        viewModel.detachChatList(roomId)
        viewModel.detachRoomInfo(roomId)

        viewModel.chatList.observe(viewLifecycleOwner){
            chatListAdapter.submitList(it)
        }

        viewModel.roomInfo.observe(viewLifecycleOwner){
            it?.let { binding.roomInfo = it }
        }
    }

    fun onClickBack(){
        findNavController().popBackStack()
    }

}