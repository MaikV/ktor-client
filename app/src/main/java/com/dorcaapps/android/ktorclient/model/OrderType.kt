package com.dorcaapps.android.ktorclient.model

import kotlinx.serialization.Serializable

@Serializable
enum class OrderType {
    MOST_RECENT_FIRST,
    MOST_RECENT_LAST
}