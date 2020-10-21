package com.dorcaapps.android.ktorclient.ui.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dorcaapps.android.ktorclient.databinding.FragmentPagingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class PagingFragment: Fragment() {
    private lateinit var binding: FragmentPagingBinding
    private val viewModel by viewModels<PagingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPagingBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.recycler.layoutManager = GridLayoutManager(context, 3)
        observeNavigation()
        return binding.root
    }

    private fun observeNavigation() {
        viewModel.navigation.consumeAsFlow()
            .onEach(findNavController()::navigate)
            .launchIn(lifecycleScope)
    }
}