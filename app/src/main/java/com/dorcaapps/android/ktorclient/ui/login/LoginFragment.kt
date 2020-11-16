package com.dorcaapps.android.ktorclient.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import com.dorcaapps.android.ktorclient.databinding.FragmentLoginBinding
import com.dorcaapps.android.ktorclient.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.isLoggedIn.observe(viewLifecycleOwner, ::navigateToPaging)
        viewModel.error.observe(viewLifecycleOwner, ::onThrowable)
        return binding.root
    }

    override fun getToolbar(): Toolbar = binding.toolbar

    private fun navigateToPaging(loggedIn: Boolean) {
        if (!loggedIn) return
        navigate(LoginFragmentDirections.actionLoginFragmentToPagingFragment())
    }

    private fun onThrowable(throwable: Throwable) {
        Toast.makeText(requireContext(), throwable.message ?: "Error occurred", Toast.LENGTH_LONG)
            .show()
    }
}