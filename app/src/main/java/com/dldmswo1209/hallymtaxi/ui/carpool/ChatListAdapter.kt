package com.dldmswo1209.hallymtaxi.ui.carpool

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.dldmswo1209.hallymtaxi.databinding.ItemExitMessageBinding
import com.dldmswo1209.hallymtaxi.databinding.ItemJoinMessageBinding
import com.dldmswo1209.hallymtaxi.databinding.ItemMyChatBinding
import com.dldmswo1209.hallymtaxi.databinding.ItemOtherChatBinding
import com.dldmswo1209.hallymtaxi.model.Chat
import com.dldmswo1209.hallymtaxi.model.User

class ChatListAdapter(
    private val currentUser: User,
) : ListAdapter<Chat, RecyclerView.ViewHolder>(diffUtil) {
    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]
        return when {
            item.joinMsg -> JOINED_MESSAGE
            item.exitMsg -> EXIT_MESSAGE
            item.userInfo.uid == currentUser.uid -> MY_CHAT
            item.userInfo.uid != currentUser.uid -> OTHER_CHAT
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

    inner class JoinMessageViewHolder(private val binding: ItemJoinMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
            binding.executePendingBindings()
        }
    }

    inner class ExitMessageViewHolder(private val binding: ItemExitMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            JOINED_MESSAGE -> {
                JoinMessageViewHolder(
                    ItemJoinMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            EXIT_MESSAGE ->{
                ExitMessageViewHolder(
                    ItemExitMessageBinding.inflate(
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
            JOINED_MESSAGE -> {
                (holder as JoinMessageViewHolder).bind(currentList[position])
            }
            EXIT_MESSAGE -> {
                (holder as ExitMessageViewHolder).bind(currentList[position])
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

        private val diffUtil = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem.chat_key == newItem.chat_key
            }

            override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
                return oldItem == newItem
            }

        }

    }


}