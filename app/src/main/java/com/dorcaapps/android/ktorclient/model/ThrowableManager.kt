package com.dorcaapps.android.ktorclient.model

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThrowableManager @Inject constructor() {
    private val _currentThrowableFlow: MutableSharedFlow<Throwable?> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val currentThrowableFlow = _currentThrowableFlow.asSharedFlow()

    fun setThrowable(throwable: Throwable?) {
        _currentThrowableFlow.tryEmit(throwable)
    }
}