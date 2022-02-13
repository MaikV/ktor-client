package com.dorcaapps.android.ktorclient.ui.login

import androidx.lifecycle.viewModelScope
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: Repository) :
    BaseViewModel() {
    val username = MutableStateFlow("Test")
    val password = MutableStateFlow("Pass")

    val loginResource = MutableStateFlow<Resource<Unit>?>(null)

    fun login() {
        repository.loginWithNewCredentials(username.value, password.value)
            .onEach {
                loginResource.value = it
            }
            .publishFailure()
            .launchIn(viewModelScope)
    }
}
