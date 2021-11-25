package com.dorcaapps.android.ktorclient.ui.paging

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PagingViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _isUploading = MutableLiveData(false)
    val isUploading = _isUploading.map { if (it) View.VISIBLE else View.GONE }

    val pagingFlow = repository
        .getPaging()
        .cachedIn(viewModelScope)

    suspend fun getThumbnail(id: Int): Bitmap {
        return repository.getThumbnailBitmap(id)
            .filterIsInstance<Resource.Success<Bitmap>>()
            .map { it.data }
            .single()
    }

//    private fun openFileChooser() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//            type = "*/*"
//            putExtra(
//                Intent.EXTRA_MIME_TYPES,
//                arrayOf(ContentType.Video.Any.toString(), ContentType.Image.Any.toString())
//            )
//            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//        }
//        startActivityForResult(intent, UPLOAD_REQUEST_CODE)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            UPLOAD_REQUEST_CODE -> uploadFiles(data)
//        }
//    }

    @OptIn(ExperimentalStdlibApi::class)
    fun uploadFiles(data: Intent?) {
        val fileUris = data?.data?.inList() ?: data?.clipData?.run {
            buildList {
                for (i in 0 until itemCount) {
                    add(getItemAt(i).uri)
                }
            }
        } ?: return
        viewModelScope.launch {
            _isUploading.value = true
            repository.uploadFiles(fileUris)
        }.invokeOnCompletion {
            _isUploading.value = false
        }
    }

    private fun Uri.inList() = listOf(this)
}
