package com.dldmswo1209.hallymtaxi.ui.carpool

import android.annotation.SuppressLint
import android.app.NotificationManager
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentChatRoomBinding
import com.dldmswo1209.hallymtaxi.model.*
import com.dldmswo1209.hallymtaxi.service.FcmService.Companion.CHANNEL_ID
import com.dldmswo1209.hallymtaxi.ui.MainActivity
import com.dldmswo1209.hallymtaxi.vm.MainViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class ChatRoomFragment: Fragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(requireActivity().application) }
    private lateinit var globalVariable: GlobalVariable

    private lateinit var room : CarPoolRoom
    private var roomInfo: RoomInfo? = null
    private var messages: MutableList<Chat> = mutableListOf()
    private lateinit var currentUser: User
    private var tokenList = mutableListOf<String?>()
    private lateinit var chatListAdapter: ChatListAdapter

    private lateinit var callback: OnBackPressedCallback
    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireContext())
    }
    private val notificationManager : NotificationManagerCompat by lazy{
        NotificationManagerCompat.from(requireActivity().applicationContext)
    }
    private var isFirst = true

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
    ): View {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        backPressedSetCallback()
        setObserver()
        registerBroadcastReceiver()
    }
    @SuppressLint("SetTextI18n")
    private fun init() {
        binding.fragment = this
        val args: ChatRoomFragmentArgs by navArgs()
        room = args.room

        globalVariable = requireActivity().application as GlobalVariable
        currentUser = globalVariable.getUser() ?: kotlin.run {
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }
        KeyboardUtils.addKeyboardToggleListener(requireActivity(), keyboardStateListener)
        setRecyclerAdapter()
        editTextWatcher()


    }
    private fun backPressedSetCallback(){
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onClickBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
    private fun setRecyclerAdapter(){
        chatListAdapter = ChatListAdapter(currentUser)
        binding.rvMessage.adapter = chatListAdapter
    }
    private fun editTextWatcher(){
        binding.etMsg.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(msg: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(msg.toString().isNotBlank()) binding.btnSend.visibility = View.VISIBLE
                else binding.btnSend.visibility = View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver(){
        viewModel.detachChatList(room.roomId)
        globalVariable.myRoom.observe(viewLifecycleOwner){ room->
            if(room == null) {
                val deletedRoomDialog = CustomDialog(
                    title = "채팅방 입장 오류",
                    content = "참여할 수 없는 채팅방입니다.",
                    positiveCallback = {onClickBack()}
                )
                deletedRoomDialog.show(parentFragmentManager, deletedRoomDialog.tag)
                return@observe
            }
            val isBefore = TimeService.isBefore(room.departureTime, "T")
            if(!isBefore && isFirst){
                // 출발시간이 지남
                val finishedRoom = CustomDialog(
                    title = "출발시간 초과",
                    content = "출발시간이 지난 채팅방 입니다.\n더 이상 카풀 목록에 표시되지 않습니다.",
                    positiveCallback = { exitRoom() },
                    positiveButton = "나가기",
                    negativeButtonVisible = true,
                    negativeButton = "취소",
                )
                finishedRoom.show(parentFragmentManager, finishedRoom.tag)
            }

            binding.room = room
            this.room = room
            tokenList = mutableListOf()

            room.participants.forEach { if(it.fcmToken != currentUser.fcmToken) tokenList.add(it.fcmToken) }
            isFirst = false
        }

        viewModel.chatList.observe(viewLifecycleOwner){
            messages = it.toMutableList()
            chatListAdapter.submitList(messages)
            scrollToLastItem()
        }

        viewModel.roomInfo.observe(viewLifecycleOwner){
            if(it == null) return@observe
            it.isNewMessage = false
            roomInfo = it
            viewModel.updateRoomInfo(it)
        }
    }
    private fun registerBroadcastReceiver() {
        // 새로운 메세지가 온 경우 브로드 캐스트 리시버를 통해 알 수 있음
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    viewModel.detachChatList(room.roomId) // 채팅 내역 조회
                }

            }, IntentFilter("newMessage"))
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
                        title = "채팅방 나가기",
                        content = "나가기를 하면 대화내용이\n모두 히스토리에 저장됩니다.\n정말 나가시겠습니까?",
                        negativeButtonVisible = true,
                        positiveButton = "나가기",
                        positiveCallback = { exitRoom() }
                    )
                    exitDialog.show(parentFragmentManager, exitDialog.tag)

                    true
                }
                else->{
                    val deactivateDialog = CustomDialog(
                        title = "카풀 마감하기",
                        content = "마감하기를 하면\n인원을 더이상 추가할 수 없습니다.\n마감할까요?",
                        negativeButtonVisible = true,
                        positiveButton = "마감하기",
                        positiveCallback = { deactivateRoomPositiveCallback() }
                    )
                    deactivateDialog.show(parentFragmentManager, deactivateDialog.tag)

                    true
                }
            }
        }
    }

    private fun deactivateRoomPositiveCallback() {
        if (room.participants.first() != currentUser) {
            Toast.makeText(requireContext(), "방장 권한입니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (room.closed) return
        val chat = Chat(
            roomId = room.roomId,
            userId = currentUser.uid,
            msg = "-카풀이 마감됐습니다-",
            messageType = CHAT_ETC
        )
        viewModel.deactivateRoom(room.roomId)
        CoroutineScope(Dispatchers.IO).launch {
            async {
                viewModel.sendMessage(chat, currentUser.name, tokenList)
            }.await()
            viewModel.detachChatList(room.roomId)
        }
    }

    private fun exitRoom() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.sendMessage(
                Chat(
                    roomId = room.roomId,
                    userId = currentUser.uid,
                    msg = "-${currentUser.name}님이 나갔습니다-",
                    messageType = CHAT_EXIT
                ), currentUser.name, tokenList
            )
            if(room.participants.first() == currentUser && room.userCount >= 2){
                // 방장이 나감
                viewModel.sendMessage(
                    Chat(
                        roomId = room.roomId,
                        userId = currentUser.uid,
                        msg = "-${room.participants[1].name}님은 이제 방장 입니다-",
                        messageType = CHAT_ETC
                    ), currentUser.name, tokenList
                )
            }
        }
        messages.forEachIndexed { idx, chat ->
            if (chat.messageType == CHAT_NORMAL && chat.userId == currentUser.uid) {
                // 유저가 해당 채팅방에서 채팅을 보낸적 있으면 히스토리에 저장함
                roomInfo?.let { roomInfo ->
                    roomInfo.isActivate = false
                    roomInfo.startPlaceName = room.startPlace.place_name
                    roomInfo.endPlaceName = room.endPlace.place_name
                    roomInfo.lastMsg = messages.last().msg
                    roomInfo.lastReceiveMsgDateTime = messages.last().dateTime
                    viewModel.insertRoomInfo(roomInfo)
                }
                return@forEachIndexed
            }
        }

        viewModel.exitRoom(currentUser, room)
        onClickBack()
    }

    fun onClickSend(){
        val msg = binding.etMsg.text.toString()
        val chat = Chat(roomId = room.roomId, userId = currentUser.uid, msg= msg, messageType = CHAT_NORMAL)

        CoroutineScope(Dispatchers.IO).launch {
            async {
                viewModel.sendMessage(chat, currentUser.name, tokenList.toList())
            }.await()
            viewModel.detachChatList(room.roomId)
        }

        binding.etMsg.text.clear()
    }

    override fun onStart() {
        super.onStart()
        globalVariable.setIsViewChatRoom(true)
        viewModel.detachRoomInfo(room.roomId)
        notificationManager.cancelAll()
    }

    override fun onPause() {
        super.onPause()
        globalVariable.setIsViewChatRoom(false)
    }

    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
        Log.d("testt", "ChatRoomFragment onDetach: ")
    }
}