package com.dorcaapps.android.ktorclient.di

import com.dorcaapps.android.ktorclient.model.bugtracker.Bugtracker
import com.dorcaapps.android.ktorclient.model.bugtracker.NoOpBugtracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BugtrackerModule {
    @Provides
    @Singleton
    fun provideBugtracker(): Bugtracker = NoOpBugtracker
}