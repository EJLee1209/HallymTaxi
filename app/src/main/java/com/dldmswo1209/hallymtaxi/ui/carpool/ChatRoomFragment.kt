package com.dldmswo1209.hallymtaxi.ui.carpool

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentChatRoomBinding
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import kotlinx.coroutines.*

class ChatRoomFragment: Fragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private lateinit var callback: OnBackPressedCallback
    private lateinit var room : CarPoolRoom
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private var messages: MutableList<Chat> = mutableListOf()
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var currentUser: User
    private var tokenList = mutableListOf<String?>()

    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireContext())
    }

    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{ // 키보드 상태(true/false)
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            viewMarginDynamicChanger.apply {
                val originalEditTextMarginBottom = MetricsUtil.convertDpToPixel(27, requireContext())
                val smallEditTextMarginBottom = MetricsUtil.convertDpToPixel(5, requireContext())

                changeConstraintMarginTopBottom(binding.inputLayout,originalEditTextMarginBottom,smallEditTextMarginBottom,0,0, isVisible)
                scrollToLastItem()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        backPressedSetCallback()
        setObserver()

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(object: BroadcastReceiver(){
            override fun onReceive(context: Context, intent: Intent) {
                viewModel.detachChatList(room.roomId)
            }

        }, IntentFilter("newMessage"))

        binding.etMsg.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(msg: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(msg.toString().isNotBlank()) binding.btnSend.visibility = View.VISIBLE
                else binding.btnSend.visibility = View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.btnSend.setOnClickListener {
            val msg = binding.etMsg.text.toString()
            val chat = Chat(roomId = room.roomId, userId = currentUser.uid, msg= msg, messageType = CHAT_NORMAL)

            CoroutineScope(Dispatchers.IO).launch {
                async {
                    viewModel.sendMessage(chat, currentUser.name, tokenList.toList())
                    delay(200)
                }.await()
                viewModel.detachChatList(room.roomId)
            }

            binding.etMsg.text.clear()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun init(){
        binding.fragment = this
        val args : ChatRoomFragmentArgs by navArgs()
        room = args.room

        currentUser = (activity as MainActivity).detachUserInfo()
        binding.tvRoomTitle.text = "${room.startPlace.place_name} - ${room.endPlace.place_name}"

        KeyboardUtils.addKeyboardToggleListener(requireActivity(), keyboardStateListener)
        chatListAdapter = ChatListAdapter(currentUser, listOf())
        binding.rvMessage.adapter = chatListAdapter

        viewModel.detachChatList(room.roomId)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver(){
        viewModel.detachRoom(room.roomId).observe(viewLifecycleOwner){room ->
            if(room == null) return@observe

            binding.tvUserCount.text = "인원 ${room.userCount}명"
            this.room = room
            val userList = listOf(room.user1, room.user2, room.user3, room.user4 )
            tokenList = mutableListOf(
                room.user1?.fcmToken,
                room.user2?.fcmToken,
                room.user3?.fcmToken,
                room.user4?.fcmToken,
            )
            if(tokenList.contains(currentUser.fcmToken)) tokenList.remove(currentUser.fcmToken)
            chatListAdapter.userList = userList
            chatListAdapter.notifyDataSetChanged()
        }

        viewModel.chatList.observe(viewLifecycleOwner){
            messages = it.toMutableList()
            Log.d("testt", "messages: ${messages}")
            chatListAdapter.submitList(messages)
            scrollToLastItem()
        }

    }

    private fun scrollToLastItem(){
        CoroutineScope(Dispatchers.Main).launch {
            async {
                delay(300)
            }.await()
            if(messages.isNotEmpty()) binding.rvMessage.scrollToPosition(messages.size-1)
        }
    }

    fun onClickBack(){
        // 채팅방을 나갈 때 마지막 메세지의 키를 저장함(히스토리에서 새로운 메세지가 왔을 때 new 를 표시하기 위함)
        if(messages.isNotEmpty()) {
            val sharedPreference =
                requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
            sharedPreference.edit().putInt("lastChatKey", messages.last().id).apply()
        }
        findNavController().navigate(R.id.action_chatRoomFragment_to_navigation_map)
    }

    fun onClickMenu(){
        val popUpMenu = PopupMenu(requireContext(), binding.btnMenu)
        requireActivity().menuInflater.inflate(R.menu.room_menu, popUpMenu.menu)
        popUpMenu.show()

        popUpMenu.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.menu_exit->{
                    val exitDialog = CustomDialog(
                        title = "채팅방 퇴장",
                        content = "정말로 나가시겠습니까?",
                        negativeButtonVisible = true,
                        positiveCallback = {
                            when(currentUser.uid){
                                room.user1?.uid ->{
                                    viewModel.exitRoom("user1", room)
                                }
                                room.user2?.uid ->{
                                    viewModel.exitRoom("user2", room)
                                }
                                room.user3?.uid ->{
                                    viewModel.exitRoom("user3", room)
                                }
                                room.user4?.uid ->{
                                    viewModel.exitRoom("user4", room)
                                }
                            }
                            messages.forEach {chat->
                                if(chat.messageType == CHAT_NORMAL && chat.userId == currentUser.uid){
                                    // 유저가 해당 채팅방에서 채팅을 보낸적 있으면 히스토리에 저장함
                                    val lastMsg = messages.last()
                                    val roomInfo = RoomInfo(room.roomId, lastMsg.msg, lastMsg.dateTime, lastMsg.id, room.startPlace, room.endPlace)
//                                    viewModel.saveHistory(roomInfo, messages)
                                    return@forEach
                                }
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.sendMessage(Chat(roomId = room.roomId, userId = currentUser.uid, msg = "${currentUser.name}님이 나갔습니다", messageType = CHAT_EXIT), currentUser.name, tokenList)
                            }
                            onClickBack()
                        }
                    )
                    exitDialog.show(parentFragmentManager, exitDialog.tag)

                    true
                }
                else->{
                    false
                }
            }
        }
    }

    private fun backPressedSetCallback(){
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onClickBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
        Log.d("testt", "ChatRoomFragment onDetach: ")
        viewModel.allListenerRemove()
    }
}