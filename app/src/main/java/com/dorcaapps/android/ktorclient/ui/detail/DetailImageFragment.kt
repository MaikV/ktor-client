package com.dorcaapps.android.ktorclient.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dorcaapps.android.ktorclient.databinding.FragmentDetailImageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailImageFragment : DetailBaseFragment() {
    private val args: DetailImageFragmentArgs by navArgs()
    private val viewModel: DetailImageViewModel by viewModels()

    private lateinit var binding: FragmentDetailImageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailImageBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageId = args.mediaId.takeIf { it > -1 }
        imageId?.let { viewModel.setImageId(imageId) }
    }

    override fun onDeleteClicked(setDeleteActionView: (layoutId: Int?) -> Unit) {
        viewModel.delete()
    }

    override fun getToolbar(): Toolbar = binding.toolbar
}
