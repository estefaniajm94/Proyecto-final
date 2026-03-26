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
import com.estef.antiphishingcoach.databinding.FragmentAuthGateBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.navigation.SharedContentViewModel
import kotlinx.coroutines.launch

class AuthGateFragment : BaseFragment<FragmentAuthGateBinding>(
    R.layout.fragment_auth_gate,
    FragmentAuthGateBinding::bind
) {

    private val sharedContentViewModel: SharedContentViewModel by activityViewModels()
    private val viewModel: AuthGateViewModel by viewModels {
        AuthGateViewModelFactory(appContainer().observeCurrentUserUseCase)
    }

    private var hasNavigated = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading || hasNavigated) return@collect
                    hasNavigated = true
                    val destinationId = when {
                        state.currentUser == null -> R.id.loginFragment
                        sharedContentViewModel.pendingSharedInput.value != null -> R.id.analyzeFragment
                        else -> R.id.homeFragment
                    }
                    findNavController().navigate(
                        destinationId,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.authGateFragment, true)
                            .build()
                    )
                }
            }
        }
    }
}
