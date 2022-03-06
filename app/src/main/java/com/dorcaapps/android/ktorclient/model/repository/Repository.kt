package com.dorcaapps.android.ktorclient.model.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.paging.PagingData
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.model.shared.MediaData
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun getMediaByteArray(id: Int): Flow<Resource<ByteArray>>
    fun getThumbnailBitmap(id: Int): Flow<Resource<Bitmap>>
    fun getPaging(): Flow<PagingData<MediaData>>
    fun uploadFilesFlow(fileUris: List<Uri>): Flow<Resource<ByteArray>>

    fun getContentLengthOfResource(uri: Uri): Flow<Long?>
    fun getBytesOfPartialResource(uri: Uri, range: IntRange): Flow<ByteArray>

    suspend fun uploadFilesInCache()
    fun delete(mediaId: Int): Flow<Resource<Unit>>
    fun loginWithNewCredentials(username: String, password: String): Flow<Resource<Unit>>
}

