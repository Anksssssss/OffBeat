package com.example.offbeat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.offbeat.databinding.OffbeatLocationItemBinding
import com.example.offbeat.models.OffbeatDetail
import com.example.offbeat.utils.OffbeatLocationsDiffCallback
import com.example.offbeat.utils.OnItemClickListener

class PostsAdapter(private val listener: OnItemClickListener):
    ListAdapter<OffbeatDetail,PostsAdapter.PostsViewHolder>(OffbeatLocationsDiffCallback()) {

   // private val locations =  mutableListOf<OffbeatDetail>()
    inner class PostsViewHolder(private val binding: OffbeatLocationItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(location: OffbeatDetail) {
            Glide.with(itemView)
                .load(location.photos[0])
                .centerCrop()
                .into(binding.photo)
            binding.apply {
                userName.text = location.userName
                locationName.text = location.locationName
                description.text = location.description
            }
            itemView.setOnClickListener {
                listener.onItemClick(location)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val binding = OffbeatLocationItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostsViewHolder(binding)
    }

//    override fun getItemCount(): Int {
//        return locations.size
//    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        //val location = locations[position]
        val location = getItem(position)
        holder.bind(location)
    }

//    fun submitList(newLocations: List<OffbeatDetail>) {
//        locations.clear()
//        locations.addAll(newLocations)
//        notifyDataSetChanged()
//    }

}