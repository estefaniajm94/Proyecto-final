package com.estef.antiphishingcoach.presentation.auth

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.databinding.FragmentAuthGateBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.collectOnStarted
import com.estef.antiphishingcoach.presentation.common.viewModelFactory
import com.estef.antiphishingcoach.presentation.navigation.SharedContentViewModel

class AuthGateFragment : BaseFragment<FragmentAuthGateBinding>(
    R.layout.fragment_auth_gate,
    FragmentAuthGateBinding::bind
) {

    private val sharedContentViewModel: SharedContentViewModel by activityViewModels()
    private val viewModel: AuthGateViewModel by viewModels {
        viewModelFactory { AuthGateViewModel(appContainer().observeCurrentUserUseCase) }
    }

    private var hasNavigated = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        collectOnStarted(viewModel.uiState) { state ->
            if (state.isLoading || hasNavigated) return@collectOnStarted
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
