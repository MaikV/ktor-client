package com.dorcaapps.android.ktorclient.model

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionManager @Inject constructor() {
    private val _currentExceptionFlow: MutableSharedFlow<Exception?> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val currentExceptionFlow = _currentExceptionFlow.asSharedFlow()

    fun setThrowable(exception: Exception?) {
        _currentExceptionFlow.tryEmit(exception)
    }
}