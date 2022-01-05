package com.dorcaapps.android.ktorclient.model.bugtracker

import android.content.Context

interface Bugtracker {
    fun init(context: Context)
    fun trackThrowable(throwable: Throwable)
}