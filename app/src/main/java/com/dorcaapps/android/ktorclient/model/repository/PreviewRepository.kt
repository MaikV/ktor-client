package com.dorcaapps.android.ktorclient.model.repository

import android.graphics.Bitmap
import android.net.Uri
import androidx.paging.PagingData
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.model.shared.MediaData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class PreviewRepository : Repository {
    override fun getMediaByteArray(id: Int): Flow<Resource<ByteArray>> = emptyFlow()

    override fun getThumbnailBitmap(id: Int): Flow<Resource<Bitmap>> = emptyFlow()

    override fun getPaging(): Flow<PagingData<MediaData>> = emptyFlow()

    override fun uploadFilesFlow(fileUris: List<Uri>): Flow<Resource<ByteArray>> = emptyFlow()
    override fun getContentLengthOfResource(uri: Uri): Flow<Long?> = emptyFlow()

    override fun getBytesOfPartialResource(uri: Uri, range: IntRange): Flow<ByteArray> = emptyFlow()

    override suspend fun uploadFilesInCache() {
    }

    override fun delete(mediaId: Int): Flow<Resource<Unit>> = emptyFlow()

    override fun loginWithNewCredentials(username: String, password: String): Flow<Resource<Unit>> =
        emptyFlow()
}