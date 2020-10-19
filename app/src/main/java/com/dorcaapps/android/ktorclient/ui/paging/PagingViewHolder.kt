package com.dorcaapps.android.ktorclient.ui.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dorcaapps.android.ktorclient.databinding.ViewholderPagingBinding

class PagingViewHolder private constructor(
    private val binding: ViewholderPagingBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(mediaData: MediaData) {
        binding.data = mediaData
    }

    companion object {
        fun create(parent: ViewGroup) = PagingViewHolder(
            ViewholderPagingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}