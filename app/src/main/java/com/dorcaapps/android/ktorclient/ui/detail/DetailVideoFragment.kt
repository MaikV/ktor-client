package com.dorcaapps.android.ktorclient.ui.detail

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dorcaapps.android.ktorclient.databinding.FragmentVideoDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailVideoFragment : Fragment() {
    private val viewModel: DetailVideoViewModel by viewModels()
    private val args: DetailVideoFragmentArgs by navArgs()
    private val currentVideoUri: Uri? = null
    private lateinit var binding: FragmentVideoDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
    }

    override fun onResume() {
        super.onResume()
        restoreVideo()
    }

    override fun onStop() {
        super.onStop()
        binding.videoView.stopPlayback()
    }

    private fun restoreVideo() {
        playVideo(currentVideoUri ?: return)
    }

    private fun playVideo(videoUri: Uri) {
        val videoView = binding.videoView
        videoView.setVideoURI(videoUri)

        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
//        videoView.start()
    }
}