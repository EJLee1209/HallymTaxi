package com.dldmswo1209.hallymtaxi.ui.carpool

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentChatRoomBinding
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.model.Chat
import com.dldmswo1209.hallymtaxi.model.User
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.ui.welcome.WelcomeActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime

class ChatRoomFragment: Fragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private lateinit var callback: OnBackPressedCallback
    private lateinit var room : CarPoolRoom
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private var messages: List<Chat> = listOf()
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var currentUser: User

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

            val chat = Chat(userInfo = currentUser, msg= msg, dateTime = LocalDateTime.now().toString())
            viewModel.sendMessage(room.roomId, chat)

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

        chatListAdapter = ChatListAdapter(currentUser)
        KeyboardUtils.addKeyboardToggleListener(requireActivity(), keyboardStateListener)
    }

    private fun setObserver(){
        viewModel.detachRoom(room.roomId).observe(viewLifecycleOwner){
            binding.tvUserCount.text = "인원 ${it.userCount}명"
            this.room = it
        }

        binding.rvMessage.adapter = chatListAdapter.apply {
            viewModel.getMessage(room.roomId).observe(viewLifecycleOwner){
                messages = getMyChatList(it)
                chatListAdapter.submitList(messages)
                scrollToLastItem()
            }
        }
    }
    private fun getMyChatList(chatList: List<Chat>) : List<Chat>{
        // 현재 유저가 입장한 시점부터 메시지 가져오기
        val myChatList = mutableListOf<Chat>()
        var startIdx = 0
        chatList.forEachIndexed { index, chat ->
            if(chat.joinMsg && chat.userInfo.uid == currentUser.uid){
                startIdx = index
                return@forEachIndexed
            }
        }
        for(i in startIdx until chatList.size){
            myChatList.add(chatList[i])
        }

        return myChatList
    }

    private fun scrollToLastItem(){
        CoroutineScope(Dispatchers.Main).launch {
            async {
                delay(300)
            }.await()
            if(messages.isNotEmpty()) binding.rvMessage.smoothScrollToPosition(messages.size-1)
        }
    }

    fun onClickBack(){
        // 채팅방을 나갈 때 마지막 메세지의 키를 저장함(히스토리에서 새로운 메세지가 왔을 때 new 를 표시하기 위함)
        if(messages.isNotEmpty()) {
            val sharedPreference =
                requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
            sharedPreference.edit().putString("lastChatKey", messages.last().chat_key).apply()
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
                                if(!chat.joinMsg && !chat.exitMsg && chat.userInfo.uid == currentUser.uid){
                                    // 유저가 해당 채팅방에서 채팅을 보낸적 있으면 히스토리에 저장함
                                    viewModel.saveHistory(room, messages)
                                    return@forEach
                                }
                            }
                            viewModel.sendMessage(room.roomId, Chat(userInfo = currentUser, msg = "${currentUser.name}님이 나갔습니다", exitMsg = true, dateTime = LocalDateTime.now().toString()))
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
        viewModel.allListenerRemove()
    }

}