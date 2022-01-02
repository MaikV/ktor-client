package com.dorcaapps.android.ktorclient.ui.destinations

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.dorcaapps.android.ktorclient.ui.paging.PagingViewModel
import io.ktor.http.ContentType
import kotlinx.coroutines.yield

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun PagedGridDestination(
    refreshTrigger: Boolean,
    onImageMediaClicked: (id: Int) -> Unit,
    onVideoMediaClicked: (id: Int) -> Unit
) {
    val viewModel: PagingViewModel = hiltViewModel()
    val mediaData = viewModel.pagingFlow.collectAsLazyPagingItems()
    LaunchedEffect(refreshTrigger) {
        mediaData.refresh()
    }
    LazyVerticalGrid(cells = GridCells.Fixed(3)) {
        items(mediaData) { item ->
            item ?: return@items
            var bitmap by remember {
                mutableStateOf<Bitmap?>(null)
            }
            LaunchedEffect(key1 = item) {
                val thumbnail = viewModel.getThumbnail(item.id)
                yield()
                bitmap = thumbnail
            }
            AnimatedContent(targetState = bitmap) { targetState ->
                if (targetState != null) {
                    Image(
                        bitmap = targetState.asImageBitmap(),
                        contentDescription = "Thumbnail",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable {
                                if (item.contentType.contentType == ContentType.Video.Any.contentType) {
                                    onVideoMediaClicked.invoke(item.id)
                                } else if (item.contentType.contentType == ContentType.Image.Any.contentType) {
                                    onImageMediaClicked.invoke(item.id)
                                }
                            }
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    span: (LazyGridItemSpanScope.(Int) -> GridItemSpan)? = null,
    itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        span = span
    ) { index ->
        itemContent(items[index])
    }
}