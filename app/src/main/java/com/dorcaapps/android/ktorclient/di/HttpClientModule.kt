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
    const val DEFAULT_REQUEST_PORT = 8080
    val DEFAULT_URL_PROTOCOL = URLProtocol.HTTP
    const val DEFAULT_HOST = "192.168.0.65"

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
            url.protocol = DEFAULT_URL_PROTOCOL
            url.port = DEFAULT_REQUEST_PORT
//            url.host = "192.168.178.21"
            url.host = DEFAULT_HOST
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
