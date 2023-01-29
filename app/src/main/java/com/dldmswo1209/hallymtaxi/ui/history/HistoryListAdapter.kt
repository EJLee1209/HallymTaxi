package com.dldmswo1209.hallymtaxi.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dldmswo1209.hallymtaxi.databinding.ItemHistoryBinding
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom

class HistoryListAdapter(
    val onClick: (CarPoolRoom)->Unit
): ListAdapter<CarPoolRoom, HistoryListAdapter.HistoryViewHolder>(diffUtil) {

    inner class HistoryViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(room: CarPoolRoom){
            binding.room = room
            binding.root.setOnClickListener {
                onClick(room)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object{
        private val diffUtil = object: DiffUtil.ItemCallback<CarPoolRoom>(){
            override fun areItemsTheSame(oldItem: CarPoolRoom, newItem: CarPoolRoom): Boolean {
                return oldItem.roomId == newItem.roomId
            }

            override fun areContentsTheSame(oldItem: CarPoolRoom, newItem: CarPoolRoom): Boolean {
                return oldItem == newItem
            }

        }
    }


}