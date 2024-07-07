package com.example.offbeat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.offbeat.databinding.ReviewItemBinding
import com.example.offbeat.models.Review

class ReviewAdapter(private var reviewsList: List<Review>):RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    class ReviewViewHolder(val binding: ReviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            binding.apply {
                userName.text = review.userName
                reviewTv.text = review.review
                timeTv.text = review.time
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ReviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reviewsList.size
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviewsList[position])
    }

    fun updateList(newList: List<Review>) {
        reviewsList = newList
        notifyDataSetChanged()
    }
}