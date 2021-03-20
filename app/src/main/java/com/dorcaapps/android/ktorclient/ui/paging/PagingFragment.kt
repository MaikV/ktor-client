package com.dorcaapps.android.ktorclient.ui.paging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dorcaapps.android.ktorclient.R
import com.dorcaapps.android.ktorclient.databinding.FragmentPagingBinding
import com.dorcaapps.android.ktorclient.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.http.ContentType

@AndroidEntryPoint
class PagingFragment : BaseFragment() {
    companion object {
        private const val UPLOAD_REQUEST_CODE = 42
    }

    private lateinit var binding: FragmentPagingBinding
    private val viewModel by viewModels<PagingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPagingBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.recycler.layoutManager = GridLayoutManager(context, 3)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        observeNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_paging, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_upload -> {
                openFileChooser()
                true
            }
            R.id.menu_item_refresh -> {
                viewModel.adapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getToolbar(): Toolbar = binding.toolbar

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(ContentType.Video.Any.toString(), ContentType.Image.Any.toString())
            )
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, UPLOAD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UPLOAD_REQUEST_CODE -> uploadFiles(data)
        }
    }

    private fun uploadFiles(data: Intent?) {
        viewModel.uploadFiles(data)
    }

    private fun observeNavigation() {
        viewModel.navigation.observe(viewLifecycleOwner, ::navigate)
    }
}
