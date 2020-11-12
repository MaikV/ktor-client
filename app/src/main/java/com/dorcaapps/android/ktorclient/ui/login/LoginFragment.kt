package com.dorcaapps.android.ktorclient.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dorcaapps.android.ktorclient.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
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

    private fun navigateToPaging(loggedIn: Boolean) {
        if (!loggedIn) return
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToPagingFragment())
    }

    private fun onThrowable(throwable: Throwable) {
        Toast.makeText(requireContext(), throwable.message ?: "Error occurred", Toast.LENGTH_LONG)
            .show()
    }
}