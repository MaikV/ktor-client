package com.dorcaapps.android.ktorclient.ui.shared

import androidx.lifecycle.ViewModel
import com.dorcaapps.android.ktorclient.model.ExceptionManager
import com.dorcaapps.android.ktorclient.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

abstract class BaseViewModel: ViewModel() {
    // Using property injection so not every single implementation of the BaseViewModel
    // has to add ThrowableManager to its constructor parameters
    @Inject
    lateinit var exceptionManager: ExceptionManager

    protected fun <T> Flow<Resource<T>>.publishFailure() = onEach {
        if (it is Resource.Failure) {
            exceptionManager.setThrowable(it.exception)
        }
    }
}