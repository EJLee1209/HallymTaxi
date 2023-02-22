package com.dldmswo1209.hallymtaxi.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dldmswo1209.hallymtaxi.data.model.Place
import com.dldmswo1209.hallymtaxi.databinding.ItemFavoriteBinding

class FavoritesListAdapter(
    val onClick: (Place) -> Unit
) : ListAdapter<Place, FavoritesListAdapter.FavoriteViewHolder>(diffUtil) {

    inner class FavoriteViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(place: Place) {
            binding.place = place
            binding.cardLayout.setOnClickListener {
                onClick(place)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        return FavoriteViewHolder(ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
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