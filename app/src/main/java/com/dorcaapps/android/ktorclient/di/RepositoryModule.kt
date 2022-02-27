package com.dorcaapps.android.ktorclient.di

import android.content.Context
import com.dorcaapps.android.ktorclient.model.AuthManager
import com.dorcaapps.android.ktorclient.model.repository.Repository
import com.dorcaapps.android.ktorclient.model.repository.RepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideRepository(
        client: HttpClient,
        authManager: AuthManager,
        @ApplicationContext context: Context
    ): Repository = RepositoryImpl(client, authManager, context)
}