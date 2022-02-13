package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.login.LoginViewModel
import com.dorcaapps.android.ktorclient.ui.shared.ProgressButton

@Composable
fun LoginDestination(onLoginValid: () -> Unit) {
    Column {
        val viewModel: LoginViewModel = hiltViewModel()
        val username by viewModel.username.collectAsState()
        TextField(
            value = username,
            onValueChange = { viewModel.username.tryEmit(it) },
            placeholder = { Text("Username") }
        )
        val password by viewModel.password.collectAsState()
        TextField(
            value = password,
            onValueChange = { viewModel.password.tryEmit(it) },
            placeholder = { Text("Password") }
        )
        val coroutineScope = rememberCoroutineScope()
        val loginState by viewModel.loginResource
            .collectAsState(context = coroutineScope.coroutineContext)
        ProgressButton(
            isLoading = loginState is Resource.Loading,
            onClick = { viewModel.login() }
        ) {
            Text("Login")
        }
        LaunchedEffect(key1 = loginState) {
            if (loginState is Resource.Success) {
                onLoginValid.invoke()
            }
        }
    }
}