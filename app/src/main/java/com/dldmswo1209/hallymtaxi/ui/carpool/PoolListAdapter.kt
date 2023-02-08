package com.dldmswo1209.hallymtaxi.ui.carpool

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dldmswo1209.hallymtaxi.R
import com.dldmswo1209.hallymtaxi.common.DistanceManager
import com.dldmswo1209.hallymtaxi.common.GlobalVariable
import com.dldmswo1209.hallymtaxi.databinding.ItemPoolBinding
import com.dldmswo1209.hallymtaxi.model.CarPoolRoom
import com.dldmswo1209.hallymtaxi.model.GENDER_OPTION_NONE
import com.dldmswo1209.hallymtaxi.model.Place

class PoolListAdapter(
    val activity: Activity,
    val endPlace: Place,
    val onClickRoom: (CarPoolRoom)->Unit,
    ): PagingDataAdapter<CarPoolRoom, PoolListAdapter.ViewHolder>(diffUtil) {

    private val globalVariable = activity.application as GlobalVariable
    var roomId = globalVariable.getMyRoomId()

    inner class ViewHolder(private val binding: ItemPoolBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(room: CarPoolRoom){
            binding.room = room
            binding.distance = DistanceManager.getDistance(room.endPlace.y, room.endPlace.x, endPlace.y, endPlace.x)
            if(roomId == room.roomId){ // 현재 참여하고 있는 방
                currentJoinedRoom()
            }else{
                normalRoom()
            }
            binding.root.setOnClickListener {
                onClickRoom(room)
            }
            binding.executePendingBindings()
        }

        private fun currentJoinedRoom(){
            binding.apply {
                itemLayout.setBackgroundResource(R.drawable.background_blue_r10)
                tvStartPlaceName.setTextColor(activity.resources.getColor(R.color.hallym_white_ffffff, activity.resources.newTheme()))
                tvEndPlaceName.setTextColor(activity.resources.getColor(R.color.hallym_white_ffffff, activity.resources.newTheme()))
                tvGenderOption.setTextColor(activity.resources.getColor(R.color.hallym_white_ffffff, activity.resources.newTheme()))
                tvStartTime.setTextColor(activity.resources.getColor(R.color.hallym_white_ffffff, activity.resources.newTheme()))
                tvUserCount.setTextColor(activity.resources.getColor(R.color.hallym_white_ffffff, activity.resources.newTheme()))
                tvDistanceTitle.setTextColor(activity.resources.getColor(R.color.hallym_white_ffffff, activity.resources.newTheme()))
                tvDistance.setTextColor(activity.resources.getColor(R.color.hallym_white_ffffff, activity.resources.newTheme()))
                ivDownRightArrow.setImageResource(R.drawable.corner_down_right_white)
            }
        }
        private fun normalRoom(){
            binding.apply {
                itemLayout.setBackgroundResource(R.drawable.background_white_r10)
                tvStartPlaceName.setTextColor(activity.resources.getColor(R.color.hallym_black_000000, activity.resources.newTheme()))
                tvEndPlaceName.setTextColor(activity.resources.getColor(R.color.hallym_black_000000, activity.resources.newTheme()))
                tvGenderOption.setTextColor(activity.resources.getColor(R.color.hallym_black_000000, activity.resources.newTheme()))
                tvStartTime.setTextColor(activity.resources.getColor(R.color.hallym_black_000000, activity.resources.newTheme()))
                tvUserCount.setTextColor(activity.resources.getColor(R.color.hallym_blue_3351b9, activity.resources.newTheme()))
                tvDistanceTitle.setTextColor(activity.resources.getColor(R.color.hallym_blue_3351b9, activity.resources.newTheme()))
                tvDistance.setTextColor(activity.resources.getColor(R.color.hallym_blue_3351b9, activity.resources.newTheme()))
                ivDownRightArrow.setImageResource(R.drawable.corner_down_right_blue)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPoolBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = getItem(position) ?: return
        holder.bind(room)
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
