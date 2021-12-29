package com.dorcaapps.android.ktorclient.di

import com.dorcaapps.android.ktorclient.model.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.URLProtocol
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpClientModule {
    private const val DEFAULT_REQUEST_PORT = 8080
    @Provides
    @Singleton
    fun provideHttpClient(authManager: AuthManager) = HttpClient(OkHttp) {
        engine {
            val loggingInterceptor =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
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
            url.port = DEFAULT_REQUEST_PORT
//            url.host = "192.168.178.21"
            url.host = "192.168.0.65"
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
