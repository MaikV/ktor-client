package com.dorcaapps.android.ktorclient.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.dorcaapps.android.ktorclient.R
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var client: HttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
//        startKtor()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host).navigateUp() || super.onSupportNavigateUp()
    }

    private fun startKtor() {
//        // Create a MediaSessionCompat
//        val mediaSession = MediaSessionCompat(this@MainActivity, "LOG_TAG").apply {
//
//            // Enable callbacks from MediaButtons and TransportControls
//            setFlags(
//                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
//                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
//            )
//
//            // Do not let MediaButtons restart the player when the app is not visible
//            setMediaButtonReceiver(null)
//
//            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
//            val stateBuilder = PlaybackStateCompat.Builder()
//                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
//            setPlaybackState(stateBuilder.build())
//
//            // MySessionCallback has methods that handle callbacks from a media controller
//            setCallback(object : MediaSessionCompat.Callback() {
//
//            })
//        }
//
//        // Create a MediaControllerCompat
//        MediaControllerCompat(this@MainActivity, mediaSession).also { mediaController ->
//            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
//        }

        client.launch(CoroutineExceptionHandler { _, throwable ->
            Log.e("MTest", "booyah", throwable)
        }) {
            Log.e("MTest", "launching...")
            getFile(client)
//            uploadFile(client)
//            tempMethod(client)


//            val videoView = findViewById<VideoView>(R.id.surface_view)
////            videoView.setVideoPath("http://192.168.178.21:8080/media/20".toUri())
//            videoView.setVideoURI("http://192.168.178.21:8080/media/20".toUri())
//
//            val mediaController = MediaController(this@MainActivity)
//            mediaController.setAnchorView(videoView)
//            videoView.setMediaController(mediaController)
//            videoView.start()
//            return@launch
            Log.e("MTest", "done...")
        }

//        client.close()
    }

    private suspend fun tempMethod(client: HttpClient) {
        val files = applicationContext.cacheDir.listFiles() ?: return
        Log.e("MTest", files.joinToString())
        val contentType = ContentType.Video.Any
        for (file in files) {
            val myFormData = formData {
                append(
                    "test",
                    file.name,
                    contentType
                ) {
                    writeFully(file.readBytes())
                }
            }
            Log.e(
                "MTest",
                file.exists().toString()
            )
            val result = client.submitFormWithBinaryData<HttpResponse>(
                formData = myFormData,
                path = "media"
            ) {
                this.method = HttpMethod.Post
            }
            Log.e("MTest", result.toString())
        }
    }

    private suspend fun getFile(client: HttpClient) {
        val response = client.get<HttpResponse>(path = "media/2")
        val responseFile = File(cacheDir, "MyTest.jpg")
        response.content.copyAndClose(responseFile.writeChannel())
    }

    private suspend fun uploadFile(client: HttpClient) {
        val filename = "Maik.jpg"
        val contentType = ContentType.Image.JPEG
//        val filename = "arrrr.mp4"
//        val contentType = ContentType.Video.MP4
        val file = File("${this@MainActivity.cacheDir.absolutePath}/$filename")
        val myFormData = formData {
            append(
                "test",
                filename,
                contentType
            ) {
                writeFully(file.readBytes())
            }
        }
        Log.e(
            "MTest",
            File("${this@MainActivity.cacheDir.absolutePath}/$filename").exists().toString()
        )
        val result = client.submitFormWithBinaryData<HttpResponse>(
            formData = myFormData,
            path = "media"
        ) {
            this.method = HttpMethod.Post
        }
        Log.e("MTest", result.toString())
    }
}