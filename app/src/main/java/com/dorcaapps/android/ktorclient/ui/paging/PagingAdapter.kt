package com.dorcaapps.android.ktorclient.ui.paging

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dorcaapps.android.ktorclient.model.Repository

class PagingAdapter(
    private val repository: Repository,
    private val onClick: (mediaData: MediaData) -> Unit
) : PagingDataAdapter<MediaData, PagingViewHolder>(getDiffCallback()) {

    override fun onBindViewHolder(holder: PagingViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
        holder.itemView.setOnClickListener { onClick(getItem(position)!!) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagingViewHolder =
        PagingViewHolder.create(parent, repository)

    override fun onViewRecycled(holder: PagingViewHolder) {
        super.onViewRecycled(holder)
        holder.onRecycle()
    }



    companion object {
        fun getDiffCallback() = object : DiffUtil.ItemCallback<MediaData>() {
            override fun areItemsTheSame(oldItem: MediaData, newItem: MediaData): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MediaData, newItem: MediaData): Boolean =
                newItem == oldItem
        }
    }
}