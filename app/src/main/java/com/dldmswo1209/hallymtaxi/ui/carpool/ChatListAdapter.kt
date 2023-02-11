package com.dldmswo1209.hallymtaxi.ui.carpool

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dldmswo1209.hallymtaxi.databinding.ItemMyChatBinding
import com.dldmswo1209.hallymtaxi.databinding.ItemOtherChatBinding
import com.dldmswo1209.hallymtaxi.databinding.ItemSystemMessageBinding
import com.dldmswo1209.hallymtaxi.model.*

// 유저리스트를 넣어주면 될듯

class ChatListAdapter(
    private val currentUser: User
) : ListAdapter<Chat, RecyclerView.ViewHolder>(diffUtil) {
    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]
        return when(item.messageType) {
            CHAT_JOIN -> JOINED_MESSAGE
            CHAT_EXIT -> EXIT_MESSAGE
            CHAT_ETC -> ETC
            CHAT_NORMAL -> if(item.userId == currentUser.uid) MY_CHAT else OTHER_CHAT
            else -> -1
        }
    }

    inner class MyViewHolder(private val binding: ItemMyChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
            binding.executePendingBindings()
        }
    }

    inner class OtherViewHolder(private val binding: ItemOtherChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
            binding.executePendingBindings()
        }
    }

    inner class SystemMessageViewHolder(private val binding: ItemSystemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
            binding.executePendingBindings()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            JOINED_MESSAGE -> {
                SystemMessageViewHolder(
                    ItemSystemMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            EXIT_MESSAGE ->{
                SystemMessageViewHolder(
                    ItemSystemMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            ETC ->{
                SystemMessageViewHolder(
                    ItemSystemMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            MY_CHAT -> {
                MyViewHolder(
                    ItemMyChatBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                OtherViewHolder(
                    ItemOtherChatBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            JOINED_MESSAGE-> {
                (holder as SystemMessageViewHolder).bind(currentList[position])
            }
            EXIT_MESSAGE ->{
                (holder as SystemMessageViewHolder).bind(currentList[position])
            }
            ETC ->{
                (holder as SystemMessageViewHolder).bind(currentList[position])
            }
            MY_CHAT -> {
                (holder as MyViewHolder).bind(currentList[position])
            }
            OTHER_CHAT -> {
                (holder as OtherViewHolder).bind(currentList[position])
            }
        }
    }

    companion object {
        private const val JOINED_MESSAGE = 0
        private const val EXIT_MESSAGE = 1
        private const val MY_CHAT = 2
        private const val OTHER_CHAT = 3
        private const val ETC = 4

        private val diffUtil = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem == newItem
            }

        }

    }


}