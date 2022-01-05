package com.dorcaapps.android.ktorclient

import android.app.Application
import com.dorcaapps.android.ktorclient.model.bugtracker.Bugtracker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class KtorClientApplication : Application() {
    @Inject lateinit var bugtracker: Bugtracker

    override fun onCreate() {
        super.onCreate()
        bugtracker.init(this)
    }
}