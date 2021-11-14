package com.dorcaapps.android.ktorclient.ui.detail

import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.MediaController
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dorcaapps.android.ktorclient.R
import com.dorcaapps.android.ktorclient.databinding.FragmentVideoDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailVideoFragment : DetailBaseFragment() {
    private val viewModel: DetailVideoViewModel by viewModels()
    private val args: DetailVideoFragmentArgs by navArgs()
    private val currentVideoUri: Uri? = null
    private lateinit var binding: FragmentVideoDetailBinding
    private var setDeleteActionView: ((layoutId: Int?) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mediaId = args.mediaId.takeIf { it > -1 } ?: return
        viewModel.setVideoId(mediaId)
        viewModel.videoUri.observe(viewLifecycleOwner, ::playVideo)
        viewModel.isDeleting.observe(viewLifecycleOwner, ::setDeleteActionView)
        binding.videoView.setOnCompletionListener {
            it.start()
        }
        binding.videoView.setOnPreparedListener {
            it.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        }
    }

    override fun onResume() {
        super.onResume()
        restoreVideo()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.windowInsetsController?.hide(WindowInsets.Type.statusBars())
            view?.setOnApplyWindowInsetsListener { _, insets ->
                insets.inset(
                    insets.getInsetsIgnoringVisibility(
                        WindowInsets.Type.systemBars()
                    )
                )
                insets
            }
            requireActivity().window.setDecorFitsSystemWindows(false)
        } else {
            requireActivity().window.decorView.systemUiVisibility =
//            view?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            requireActivity().window.addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onStop() {
        super.onStop()
        binding.videoView.stopPlayback()
    }

    override fun onDeleteClicked(setDeleteActionView: (layoutId: Int?) -> Unit) {
        this.setDeleteActionView = setDeleteActionView
        viewModel.delete()
    }

    override fun getToolbar(): Toolbar = binding.toolbar

    private fun setDeleteActionView(isDeleting: Boolean) {
        val setDeleteActionView = setDeleteActionView ?: return
        if (isDeleting) {
            setDeleteActionView(R.layout.action_view_progress_bar)
        } else {
            setDeleteActionView(null)
        }
    }

    private fun restoreVideo() {
//        playVideo(currentVideoUri ?: return)
    }

    private fun playVideo(videoUri: Uri) {
        val videoView = binding.videoView
        videoView.setVideoURI(videoUri)

        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.start()
    }
}
