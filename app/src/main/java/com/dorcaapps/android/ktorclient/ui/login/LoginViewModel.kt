package com.dorcaapps.android.ktorclient.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: Repository) :
    ViewModel() {
    val username = MutableLiveData("")
    val password = MutableLiveData("")

    private val loginResource = MutableStateFlow<Resource<Unit>?>(null)

    val isLoading =
        loginResource.map { it is Resource.Loading }.asLiveData(viewModelScope.coroutineContext)
    val isLoggedIn =
        loginResource.map { it is Resource.Success }.asLiveData(viewModelScope.coroutineContext)
    val error = loginResource.filterIsInstance<Resource.Error>().map { it.throwable }
        .asLiveData(viewModelScope.coroutineContext)

    fun login() {
        repository.loginWithNewCredentials(username.value!!, password.value!!)
            .onEach { loginResource.value = it }
            .launchIn(viewModelScope)
    }
}
