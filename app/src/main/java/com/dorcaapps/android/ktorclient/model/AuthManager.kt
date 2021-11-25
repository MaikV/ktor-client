package com.dorcaapps.android.ktorclient.model

import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {
    private lateinit var auth: Auth
    var username = ""
    var password = ""
    val authConfig: Auth.() -> Unit = {
        this@AuthManager.auth = this
        reloadAuthProvider()
    }

    fun reloadAuthProvider() {
        auth.providers.clear()
        auth.apply {
            digest {
                credentials {
                    DigestAuthCredentials(
                        this@AuthManager.username,
                        this@AuthManager.password
                    )
                }
                realm = "login"
            }
        }
    }
}