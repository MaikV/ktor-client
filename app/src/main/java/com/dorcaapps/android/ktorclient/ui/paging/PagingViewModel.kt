package com.dorcaapps.android.ktorclient.ui.paging

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.model.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import javax.inject.Inject

@HiltViewModel
class PagingViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val pagingFlow = repository
        .getPaging()
        .cachedIn(viewModelScope)

    suspend fun getThumbnail(id: Int): Bitmap {
        return repository.getThumbnailBitmap(id)
            .filterIsInstance<Resource.Success<Bitmap>>()
            .map { it.data }
            .single()
    }
}
