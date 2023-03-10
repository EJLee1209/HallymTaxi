package com.dldmswo1209.hallymtaxi.ui.carpool

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.*
import com.dldmswo1209.hallymtaxi.common.keyboard.KeyboardUtils
import com.dldmswo1209.hallymtaxi.data.UiState
import com.dldmswo1209.hallymtaxi.data.model.*
import com.dldmswo1209.hallymtaxi.databinding.FragmentChatRoomBinding
import com.dldmswo1209.hallymtaxi.ui.MainViewModel
import com.dldmswo1209.hallymtaxi.ui.SplashActivity
import com.dldmswo1209.hallymtaxi.ui.dialog.CustomDialog
import com.dldmswo1209.hallymtaxi.ui.dialog.LoadingDialog
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class ChatRoomFragment: Fragment() {
    private lateinit var binding: FragmentChatRoomBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var myApplication: MyApplication

    private lateinit var room : CarPoolRoom
    private var roomInfo: RoomInfo? = null
    private var messages: MutableList<Chat> = mutableListOf()
    private lateinit var currentUser: User
    private var myToken = ""
    private var tokenList = mutableListOf<String?>()
    private lateinit var chatListAdapter: ChatListAdapter

    private val viewMarginDynamicChanger : ViewMarginDynamicChanger by lazy{
        ViewMarginDynamicChanger(requireActivity())
    }
    private val notificationManager : NotificationManagerCompat by lazy{
        NotificationManagerCompat.from(requireActivity().applicationContext)
    }
    private val loadingDialog by lazy{
        LoadingDialog(requireActivity())
    }

    private var isFirst = true
    private var mLastClickTime = 0L
    private var performExit = false

    private val keyboardStateListener = object: KeyboardUtils.SoftKeyboardToggleListener{ // ????????? ??????(true/false)
        override fun onToggleSoftKeyboard(isVisible: Boolean) {
            viewMarginDynamicChanger.apply {
                changeConstraintMarginTopBottom(binding.inputLayout,27.dp,5.dp,0,0, isVisible)
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
        registerBackPressedCallback(R.id.action_chatRoomFragment_to_navigation_map)
        setObserver()
        registerBroadcastReceiver()
    }
    @SuppressLint("SetTextI18n")
    private fun init() {
        binding.fragment = this
        val args: ChatRoomFragmentArgs by navArgs()
        room = args.room

        myApplication = requireActivity().application as MyApplication
        currentUser = myApplication.getUser() ?: kotlin.run {
            startActivity(Intent(requireActivity(), SplashActivity::class.java))
            requireActivity().finish()
            return
        }
        myToken = myApplication.getFcmToken()

        KeyboardUtils.addKeyboardToggleListener(requireActivity(), keyboardStateListener)
        setRecyclerAdapter()
        editTextWatcher()

    }
    private fun setRecyclerAdapter(){
        chatListAdapter = ChatListAdapter(currentUser){ chatId->
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.deleteChat(chatId).join()
                viewModel.detachChatList(room.roomId)
            }
        }
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
        myApplication.myRoom.observe(viewLifecycleOwner){ room->
            if(performExit) return@observe

            if(room.roomId.isBlank()) {
                val deletedRoomDialog = CustomDialog(
                    title = "????????? ?????? ??????",
                    content = "????????? ??? ?????? ??????????????????.",
                    positiveCallback = {onClickBack()}
                )
                deletedRoomDialog.show(parentFragmentManager, deletedRoomDialog.tag)
                return@observe
            }


            val isBefore = TimeService.isBefore(room.departureTime, "T")
            if(!isBefore && isFirst){
                // ??????????????? ??????
                val finishedRoom = CustomDialog(
                    title = "???????????? ??????",
                    content = "??????????????? ?????? ????????? ?????????.\n??? ?????? ?????? ????????? ???????????? ????????????.",
                    positiveCallback = { viewModel.exitRoom(room) },
                    positiveButton = "?????????",
                    negativeButtonVisible = true,
                    negativeButton = "??????",
                )
                finishedRoom.show(parentFragmentManager, finishedRoom.tag)
            }

            binding.room = room
            this.room = room

            isFirst = false
        }
        viewModel.subscribeParticipantsTokens.observe(viewLifecycleOwner) { tokens ->
            tokenList = tokens.toMutableList()
            tokenList.remove(myToken)
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

        viewModel.sendPush.observe(viewLifecycleOwner){ state->
            when(state){
                is UiState.Loading ->{
                    viewModel.detachChatList(room.roomId)
                }
                is UiState.Failure ->{
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.updateChatById(state.error!!, SEND_STATE_FAIL).join()
                        viewModel.detachChatList(room.roomId)
                    }
                }
                is UiState.Success ->{
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.updateChatById(state.data, SEND_STATE_SUCCESS).join()
                        viewModel.detachChatList(room.roomId)
                    }
                }
            }
        }

        viewModel.deactivateRoom.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure ->{
                    loadingDialog.dismiss()
                    toast(state.error ?: "??? ??? ?????? ?????? ?????????")
                }
                is UiState.Success ->{
                    loadingDialog.dismiss()
                    val chat = Chat(
                        roomId = room.roomId,
                        userId = currentUser.uid,
                        msg = "????????? ??????????????????",
                        messageType = CHAT_ETC
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.sendMessage(chat, currentUser.name, tokenList).join()
                        viewModel.detachChatList(room.roomId)
                    }
                }
            }
        }

        viewModel.exitRoom.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    performExit = true
                    loadingDialog.show()
                    messages.forEachIndexed { idx, chat ->
                        if (chat.messageType == CHAT_NORMAL && chat.userId == currentUser.uid) {
                            // ????????? ?????? ??????????????? ????????? ????????? ????????? ??????????????? ?????????
                            roomInfo?.let { roomInfo ->
                                roomInfo.isActivate = false
                                roomInfo.startPlaceName = room.startPlace.place_name
                                roomInfo.endPlaceName = room.endPlace.place_name
                                roomInfo.lastMsg = messages.last().msg
                                roomInfo.lastReceiveMsgDateTime = messages.last().dateTime
                                viewModel.insertRoomInfo(roomInfo)
                                return@forEachIndexed
                            }
                        }
                    }
                }
                is UiState.Failure ->{ }
                is UiState.Success ->{
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.sendMessage(
                            Chat(
                                roomId = room.roomId,
                                userId = currentUser.uid,
                                msg = "${currentUser.name}?????? ???????????????",
                                messageType = CHAT_EXIT
                            ),
                            currentUser.name,
                            tokenList
                        ).join()
                        if(room.participants.first() == currentUser.uid && room.userCount >= 2){
                            // ????????? ??????
                            viewModel.findUserName(room.participants[1]) // ????????? ???????????? ????????? ????????? ?????????
                        }else {
                            withContext(Dispatchers.Main){
                                loadingDialog.dismiss()
                                onClickBack()
                            }
                        }
                    }
                }
            }
        }

        viewModel.findUserName.observe(viewLifecycleOwner) { state ->
            when(state) {
                is UiState.Loading -> {
                    loadingDialog.show()
                }
                is UiState.Failure -> {}
                is UiState.Success -> {
                    val name = state.data
                    val chat = Chat(
                        roomId = room.roomId,
                        userId = currentUser.uid,
                        msg = "$name ?????? ?????? ?????????",
                        messageType = CHAT_ETC
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.sendMessage(
                            chat = chat,
                            userName = currentUser.name,
                            receiveTokens = tokenList
                        ).join()
                        withContext(Dispatchers.Main){
                            loadingDialog.dismiss()
                            onClickBack()
                        }
                    }
                }
            }
        }

        viewModel.detachChatList(room.roomId)
        viewModel.subscribeParticipantsTokens(room.roomId)
    }
    private fun registerBroadcastReceiver() {
        // ????????? ???????????? ??? ?????? ????????? ????????? ???????????? ?????? ??? ??? ??????
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    viewModel.detachChatList(room.roomId) // ?????? ?????? ??????
                }

            }, IntentFilter("newMessage"))
    }

    private fun scrollToLastItem(){
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                delay(300)
            }
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
                        title = "????????? ?????????",
                        content = "???????????? ?????? ???????????????\n?????? ??????????????? ???????????????.\n?????? ??????????????????????",
                        negativeButtonVisible = true,
                        positiveButton = "?????????",
                        positiveCallback = { viewModel.exitRoom(room) }
                    )
                    exitDialog.show(parentFragmentManager, exitDialog.tag)

                    true
                }
                R.id.menu_deactivate -> {
                    val deactivateDialog = CustomDialog(
                        title = "?????? ????????????",
                        content = "??????????????? ??????\n????????? ????????? ????????? ??? ????????????.\n????????????????",
                        negativeButtonVisible = true,
                        positiveButton = "????????????",
                        positiveCallback = { deactivateRoomPositiveCallback() }
                    )
                    deactivateDialog.show(parentFragmentManager, deactivateDialog.tag)

                    true
                }
                R.id.menu_share -> {
                    val shareText = "${room.startPlace.place_name} -> ${room.endPlace.place_name}\n\n?????? ?????? ??? ?????? ????????????!\n???????????? : ${TimeService.parsingDepartureTime(room.departureTime)}\n?????? ?????? : ${room.userCount}/${room.userMaxCount}\n\n??????????????? ?????? ??? ????????? ????????? ?????? ????????? ???????????? ????????? ???????????????!\n??? ?????? ????????? ?????? ?????????????????????."
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    startActivity(Intent.createChooser(intent, "?????? ?????? ??????"))
                    true
                }
                else -> true
            }
        }
    }

    private fun deactivateRoomPositiveCallback() {
        if (room.participants.first() != currentUser.uid) {
            Toast.makeText(requireContext(), "?????? ???????????????.", Toast.LENGTH_SHORT).show()
            return
        }
        if (room.closed) return

        viewModel.deactivateRoom(room.roomId)
    }

    fun onClickSend(){
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        val msg = binding.etMsg.text.toString()
        if(msg.isBlank()) return

        val chat = Chat(roomId = room.roomId, userId = currentUser.uid, userName = currentUser.name ,msg= msg, messageType = CHAT_NORMAL)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.sendMessage(chat, currentUser.name, tokenList)
        }
        binding.etMsg.text.clear()
    }

    override fun onStart() {
        super.onStart()
        myApplication.setIsViewChatRoom(true)
        viewModel.detachRoomInfo(room.roomId)
        notificationManager.cancelAll()
    }

    override fun onPause() {
        super.onPause()
        myApplication.setIsViewChatRoom(false)
    }

    override fun onDetach() {
        super.onDetach()
        KeyboardUtils.removeKeyboardToggleListener(keyboardStateListener)
    }
}