package com.dorcaapps.android.ktorclient.model.repository

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

@OptIn(UnstableApi::class)
class VideoStreamingDataSource(
    private val getContentLengthOfResource: (Uri) -> Flow<Long?>,
    private val getBytesOfPartialResource: (Uri, IntRange) -> Flow<ByteArray>
) : BaseDataSource(true) {
    private var internalOffset = 0
    private var downloadJob: Job? = null
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private var internalDataSpec: DataSpec? = null
    private var internalBuffer: Array<Byte?>? = null
    private val bufferFlow: MutableSharedFlow<Array<Byte?>> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val allNonNull = (internalOffset until internalOffset + length).all { index ->
            internalBuffer!![index] != null
        }
        val read = if (allNonNull) {
            val relevantSlice = internalBuffer!!
                .sliceArray(internalOffset until internalOffset + length)
            relevantSlice
                .requireNoNulls()
                .forEachIndexed { index, byte ->
                    buffer[offset + index] = byte
                }
            relevantSlice.size
        } else {
            runBlocking(dispatcher) {
                val relevantSlice = bufferFlow
                    .map {
                        it.sliceArray(internalOffset until internalOffset + length)
                    }
                    .first { slice -> slice.all { it != null } }
                    .requireNoNulls()
                relevantSlice.forEachIndexed { index, byte ->
                    buffer[offset + index] = byte
                }
                relevantSlice.size
            }
        }
        bytesTransferred(read)
        internalOffset += read
        return read
    }

    override fun open(dataSpec: DataSpec): Long {
        val position = dataSpec.position.toInt()
        internalOffset = position
        transferInitializing(dataSpec)

        val contentLength = runBlocking(dispatcher) {
            getContentLengthOfResource(dataSpec.uri).single() ?: -1
        }

        if (dataSpec.uri != internalDataSpec?.uri) {
            internalBuffer = arrayOfNulls(contentLength.toInt())
            bufferFlow.tryEmit(internalBuffer ?: return -1)
        }
        internalDataSpec = dataSpec
        transferStarted(dataSpec)
        downloadJob = createDownloadJob(dataSpec, internalBuffer ?: return -1)
        return contentLength - position
    }

    override fun getUri(): Uri? = internalDataSpec?.uri

    override fun close() {
        transferEnded()
        downloadJob?.cancel()
        downloadJob = null
    }

    @kotlin.OptIn(DelicateCoroutinesApi::class)
    private fun createDownloadJob(
        dataSpec: DataSpec,
        byteArray: Array<Byte?>
    ) = GlobalScope.launch(dispatcher) {
        val nullRanges = getNullRangesFromPosition()
        yield()
        for (nullRange in nullRanges) {
            var offset = nullRange.first
            getBytesOfPartialResource(dataSpec.uri, nullRange).collect { bytes ->
                bytes.forEach { byte ->
                    byteArray[offset++] = byte
                }
                yield()
                bufferFlow.emit(byteArray)
            }
        }
    }

    private fun getNullRangesFromPosition(): List<IntRange> {
        val nullRanges = mutableListOf<IntRange>()
        var currentRangeStart: Int? = null
        (internalDataSpec!!.position.toInt() until internalBuffer!!.size).forEach { index ->
            val byte = internalBuffer!![index]
            when {
                byte == null && currentRangeStart == null -> currentRangeStart = index
                byte != null && currentRangeStart != null -> {
                    nullRanges.add(currentRangeStart!! until index)
                    currentRangeStart = null
                }
                index == internalBuffer!!.lastIndex && currentRangeStart != null ->
                    nullRanges.add(currentRangeStart!!..index)
            }
        }
        return nullRanges
    }
}