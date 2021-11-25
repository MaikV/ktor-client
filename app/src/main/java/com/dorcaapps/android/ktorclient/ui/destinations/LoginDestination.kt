package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.login.LoginViewModel

@Composable
fun LoginDestination(onLoginValid: () -> Unit) {
    Column {
        var username by remember {
            mutableStateOf("Test")
        }
        TextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Username") }
        )
        var password by remember {
            mutableStateOf("Pass")
        }
        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") }
        )
        val viewModel: LoginViewModel = hiltViewModel()
        Button(onClick = {
            viewModel.login(username, password)
        }) {
            Text("Login")
        }
        val coroutineScope = rememberCoroutineScope()
        val loginState by viewModel.loginResource
            .collectAsState(context = coroutineScope.coroutineContext)
        LaunchedEffect(key1 = loginState) {
            if (loginState is Resource.Success) {
                onLoginValid.invoke()
            }
        }
    }
}