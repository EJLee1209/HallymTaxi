package com.dldmswo1209.hallymtaxi.ui.carpool

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.databinding.ItemPoolBinding
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom

class PoolListAdapter(
    val joinedRoom : CarPoolRoom?,
    val context: Context,
    val distanceList: List<Int>,
    val onClickRoom: (CarPoolRoom)->Unit
    ): ListAdapter<CarPoolRoom, PoolListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemPoolBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(room: CarPoolRoom){
            binding.room = room
            binding.distance = distanceList[adapterPosition]
            if(joinedRoom?.roomId == room.roomId){ // 현재 참여하고 있는 방
                currentJoinedRoom()
            }
            binding.root.setOnClickListener {
                onClickRoom(room)
            }
            binding.executePendingBindings()
        }

        private fun currentJoinedRoom(){
            binding.apply {
                itemLayout.setBackgroundResource(R.drawable.background_blue_r10)
                tvStartPlaceName.setTextColor(context.resources.getColor(R.color.hallym_white_ffffff, context.resources.newTheme()))
                tvEndPlaceName.setTextColor(context.resources.getColor(R.color.hallym_white_ffffff, context.resources.newTheme()))
                tvGenderOption.setTextColor(context.resources.getColor(R.color.hallym_white_ffffff, context.resources.newTheme()))
                tvStartTime.setTextColor(context.resources.getColor(R.color.hallym_white_ffffff, context.resources.newTheme()))
                tvUserCount.setTextColor(context.resources.getColor(R.color.hallym_white_ffffff, context.resources.newTheme()))
                tvDistance.setTextColor(context.resources.getColor(R.color.hallym_white_ffffff, context.resources.newTheme()))
                ivDownRightArrow.setImageResource(R.drawable.corner_down_right_white)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPoolBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
