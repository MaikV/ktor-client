package com.dorcaapps.android.ktorclient.extensions

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

@BindingAdapter("adapter")
fun setAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    recyclerView.adapter = adapter
}

@BindingAdapter("layoutManager")
fun setLayoutManager(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager) {
    recyclerView.layoutManager = layoutManager
}

@BindingAdapter("glideImage")
fun setGlideImage(imageView: ImageView, url: String) {
    Glide.with(imageView)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(imageView)
}

@BindingAdapter("isVisible")
fun setVisibility(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}