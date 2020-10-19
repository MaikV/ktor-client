package com.dorcaapps.android.ktorclient.extensions

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

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
//    Glide.with(imageView)
//        .load(url)
//        .diskCacheStrategy(DiskCacheStrategy.NONE)
//        .into(imageView)
}