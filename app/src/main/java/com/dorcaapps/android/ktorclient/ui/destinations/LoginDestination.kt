package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.model.repository.PreviewRepository
import com.dorcaapps.android.ktorclient.ui.login.LoginViewModel
import com.dorcaapps.android.ktorclient.ui.shared.ProgressButton

@Composable
fun LoginDestination(viewModel: LoginViewModel = hiltViewModel(), onLoginValid: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                modifier = Modifier.fillMaxWidth(),
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
}

@Preview
@Composable
fun LoginDestination_Preview() {
    val vm = LoginViewModel(PreviewRepository())
    LoginDestination(vm) {

    }
}
