package com.example.offbeat.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.offbeat.models.OffbeatDetail

class OffbeatLocationsDiffCallback : DiffUtil.ItemCallback<OffbeatDetail>() {
    override fun areItemsTheSame(oldItem: OffbeatDetail, newItem: OffbeatDetail): Boolean {
        return oldItem.offBeatId == newItem.offBeatId
    }

    override fun areContentsTheSame(oldItem: OffbeatDetail, newItem: OffbeatDetail): Boolean {
        return oldItem == newItem
    }

}