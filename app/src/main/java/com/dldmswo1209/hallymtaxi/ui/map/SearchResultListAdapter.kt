package com.dldmswo1209.hallymtaxi.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dldmswo1209.hallymtaxi.databinding.ItemSearchResultBinding
import com.dldmswo1209.hallymtaxi.data.model.Place

class SearchResultListAdapter(val onClick: (Place)->Unit): ListAdapter<Place, SearchResultListAdapter.ResultViewHolder>(diffUtil) {

    inner class ResultViewHolder(private val binding: ItemSearchResultBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(place: Place){
            binding.root.setOnClickListener{
                onClick(place)
            }

            binding.place = place
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        return ResultViewHolder(ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object{
        private val diffUtil = object: DiffUtil.ItemCallback<Place>(){
            override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem.road_address_name == newItem.road_address_name
            }

            override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem == newItem
            }

        }
    }

}