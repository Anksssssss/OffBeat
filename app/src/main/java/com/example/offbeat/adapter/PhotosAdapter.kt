package com.example.offbeat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.offbeat.databinding.PhotoItemBinding

class PhotosAdapter(private val photos: MutableList<String>):RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(private val binding: PhotoItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(url: String){
            Glide.with(itemView)
                .load(url)
                .centerCrop()
                .into(binding.image)
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoViewHolder {
        val binding = PhotoItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotosAdapter.PhotoViewHolder, position: Int) {
       val url = photos[position]
        holder.bind(url)
    }

    override fun getItemCount(): Int {
       return photos.size
    }

}