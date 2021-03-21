package com.dorcaapps.android.ktorclient.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dorcaapps.android.ktorclient.model.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DetailImageViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private var imageId: Int? = null

    fun setImageId(id: Int) {
        imageId = id
    }

    fun delete() {
        val imageId = imageId ?: return
        repository.delete(imageId).onEach { }.launchIn(viewModelScope)
    }
}