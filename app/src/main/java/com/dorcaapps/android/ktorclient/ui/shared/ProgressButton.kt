package com.dorcaapps.android.ktorclient.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ProgressButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        Button(modifier = modifier, onClick = onClick, enabled = !isLoading) {
            content()
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .matchParentSize()
                    .aspectRatio(1f, true)
                    .padding(8.dp)
            )
        }
    }
}

@Preview
@Composable
fun ProgressButton_Preview() {
    ProgressButton(isLoading = true, onClick = { }) {
        Text("Click")
    }
}