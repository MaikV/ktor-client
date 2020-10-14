package com.dorcaapps.android.ktorclient

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

data class TestEntity(val myInt: Int, val myString: String)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startKtor()
    }

    private fun startKtor() {
        val client = HttpClient(OkHttp) {
            engine {
                val loggingInterceptor =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                addInterceptor(loggingInterceptor)
            }
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
            install(DefaultRequest) {
                url.protocol = URLProtocol.HTTP
                url.port = 8080
                url.host = "192.168.178.21"
            }
            /** Logging breaks file upload, using HttpLoggingInterceptor instead */
//            install(Logging) {
//                logger = Logger.DEFAULT
//                level = LogLevel.ALL
//            }
        }
        client.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e("MTest", "booyah", throwable)
        }) {
            Log.e("MTest", "launching...")
            val response = client.get<HttpResponse>(path = "getfile")
            val responseFile = File(cacheDir, "MyTest.jpg")
            response.content.copyAndClose(responseFile.writeChannel())
            return@launch
            val file = File("${this@MainActivity.cacheDir.absolutePath}/Maik.jpg")
            val myFormData = formData {
                append("text", "Hello, world")
                append(
                    "test",
                    "Maik.jpg",
                    ContentType.Image.JPEG
                ) {
                    writeFully(file.readBytes())
                }
            }
            Log.e(
                "MTest",
                File("${this@MainActivity.cacheDir.absolutePath}/Maik.jpg").exists().toString()
            )
            val result = client.submitFormWithBinaryData<HttpResponse>(
                formData = myFormData,
                path = "getfile"
            ) {}
            Log.e("MTest", result.toString())
            Log.e("MTest", "done...")
        }

//        client.close()
    }
}