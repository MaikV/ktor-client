package com.dorcaapps.android.ktorclient.model.bugtracker

import android.content.Context

object NoOpBugtracker : Bugtracker {
    override fun init(context: Context) {

    }

    override fun trackThrowable(throwable: Throwable) {

    }
}