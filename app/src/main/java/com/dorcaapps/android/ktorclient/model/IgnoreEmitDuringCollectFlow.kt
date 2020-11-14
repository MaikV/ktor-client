package com.dorcaapps.android.ktorclient.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive

class IgnoreEmitDuringCollectFlow<T> : MutableSharedFlow<T> {
    @ExperimentalCoroutinesApi
    override fun resetReplayCache(): Unit = TODO("Not yet implemented")
    override suspend fun emit(value: T) = TODO("Not yet implemented")
    private val _replayCache = listOf<T>()
    private val _subscriptionCount = MutableStateFlow(0)

    private var emitChannel = Channel<T>(Channel.CONFLATED)
    private var isCollecting = false

    override val subscriptionCount: StateFlow<Int> = _subscriptionCount
    override val replayCache: List<T> = _replayCache

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) {
        require(subscriptionCount.value == 0)
        _subscriptionCount.value = 1
        while (currentCoroutineContext().isActive) {
            val valueToEmit = emitChannel.receive()
            isCollecting = true
            collector.emit(valueToEmit)
            isCollecting = false
        }
        _subscriptionCount.value = 0
    }

    override fun tryEmit(value: T): Boolean =
        if (isCollecting) false else emitChannel.offer(value)
}