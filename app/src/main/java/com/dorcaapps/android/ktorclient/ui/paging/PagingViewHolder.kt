package com.dorcaapps.android.ktorclient.ui.paging

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dorcaapps.android.ktorclient.databinding.ViewholderPagingBinding
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PagingViewHolder private constructor(
    private val binding: ViewholderPagingBinding,
    private val repository: Repository
): RecyclerView.ViewHolder(binding.root) {

    private lateinit var coroutineScope: CoroutineScope

    fun bind(mediaData: MediaData) {
        binding.data = mediaData
        loadThumbnailWith(mediaData.id)
    }

    fun onRecycle() {
        coroutineScope.cancel()
        binding.previewImage.setImageBitmap(null)
    }

    private fun loadThumbnailWith(id: Int) {
        coroutineScope = MainScope()
        repository.getThumbnailFileUri(id)
            .filterIsInstance<Resource.Success<Bitmap>>()
            .onEach {
                binding.previewImage.setImageBitmap(it.data)
            }
            .launchIn(coroutineScope)
    }

    companion object {
        fun create(parent: ViewGroup, repository: Repository) = PagingViewHolder(
            ViewholderPagingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            repository
        )
    }
}