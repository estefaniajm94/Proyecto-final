package com.estef.antiphishingcoach.presentation.auth

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentLoginBinding
import com.estef.antiphishingcoach.presentation.common.AndroidStringResolver
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.navigation.SharedContentViewModel
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment<FragmentLoginBinding>(
    R.layout.fragment_login,
    FragmentLoginBinding::bind
) {

    private val sharedContentViewModel: SharedContentViewModel by activityViewModels()
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            loginLocalUserUseCase = appContainer().loginLocalUserUseCase,
            stringResolver = AndroidStringResolver(requireContext().applicationContext)
        )
    }

    private var hasNavigated = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        binding.btnLogin.setOnClickListener {
            viewModel.login(
                email = binding.etEmail.text?.toString().orEmpty(),
                password = binding.etPassword.text?.toString().orEmpty()
            )
        }
        binding.btnOpenRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressLogin.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
                    binding.tilEmail.error = state.emailError
                    binding.tilPassword.error = state.passwordError
                    binding.tvLoginStatus.text = state.statusMessage.orEmpty()

                    if (state.authenticatedUser != null && !hasNavigated) {
                        hasNavigated = true
                        val destinationId = if (sharedContentViewModel.pendingSharedInput.value != null) {
                            R.id.analyzeFragment
                        } else {
                            R.id.homeFragment
                        }
                        findNavController().navigate(
                            destinationId,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph, true)
                                .build()
                        )
                    }
                }
            }
        }
    }
}
