package com.dorcaapps.android.ktorclient.ui.shared

import androidx.compose.material.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.dorcaapps.android.ktorclient.model.ExceptionManager

@Composable
fun ExceptionDialog(exceptionManager: ExceptionManager) {
    val currentException by exceptionManager.currentExceptionFlow.collectAsState(
        initial = null
    )
    currentException?.let {
        AlertDialog(
            onDismissRequest = { exceptionManager.setThrowable(null) },
            confirmButton = {
                TextButton(onClick = { exceptionManager.setThrowable(null) }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            title = { Text("There was an error") },
            text = { Text(it.message ?: "Unknown error") })
    }
}