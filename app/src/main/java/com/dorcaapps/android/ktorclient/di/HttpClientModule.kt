package com.dorcaapps.android.ktorclient.di

import com.dorcaapps.android.ktorclient.model.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object HttpClientModule {
    @Provides
    @Singleton
    fun provideHttpClient(authManager: AuthManager) = HttpClient(OkHttp) {
        engine {
            val loggingInterceptor =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            addInterceptor(loggingInterceptor)
            config {
                retryOnConnectionFailure(true)
            }
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(DefaultRequest) {
            url.protocol = URLProtocol.HTTP
            url.port = 8080
//            url.host = "192.168.178.21"
            url.host = "192.168.178.29"
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(Auth, authManager.authConfig)
        /** Logging breaks file upload, using HttpLoggingInterceptor instead */
//            install(Logging) {
//                logger = Logger.DEFAULT
//                level = LogLevel.ALL
//            }
    }
}