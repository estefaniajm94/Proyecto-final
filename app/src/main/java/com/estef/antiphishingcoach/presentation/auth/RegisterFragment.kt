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
import com.estef.antiphishingcoach.databinding.FragmentRegisterBinding
import com.estef.antiphishingcoach.presentation.avatar.AvatarPickerDialogFragment
import com.estef.antiphishingcoach.presentation.common.AndroidStringResolver
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.renderAvatar
import com.estef.antiphishingcoach.presentation.navigation.SharedContentViewModel
import kotlinx.coroutines.launch

class RegisterFragment : BaseFragment<FragmentRegisterBinding>(
    R.layout.fragment_register,
    FragmentRegisterBinding::bind
) {
    private val avatarPickerRequestKey = "register_avatar_picker"

    private val sharedContentViewModel: SharedContentViewModel by activityViewModels()
    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(
            registerLocalUserUseCase = appContainer().registerLocalUserUseCase,
            stringResolver = AndroidStringResolver(requireContext().applicationContext)
        )
    }

    private var hasNavigated = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        parentFragmentManager.setFragmentResultListener(
            avatarPickerRequestKey,
            viewLifecycleOwner
        ) { _, result ->
            val avatarId = result.getString(AvatarPickerDialogFragment.RESULT_AVATAR_ID) ?: return@setFragmentResultListener
            viewModel.onAvatarSelected(avatarId)
        }
        binding.btnChangeAvatar.setOnClickListener {
            AvatarPickerDialogFragment.newInstance(
                requestKey = avatarPickerRequestKey,
                selectedAvatarId = viewModel.uiState.value.selectedAvatarId
            ).show(parentFragmentManager, AvatarPickerDialogFragment.TAG)
        }
        binding.btnCreateAccount.setOnClickListener {
            viewModel.register(
                displayName = binding.etDisplayName.text?.toString().orEmpty(),
                email = binding.etEmail.text?.toString().orEmpty(),
                password = binding.etPassword.text?.toString().orEmpty(),
                confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()
            )
        }
        binding.btnOpenLogin.setOnClickListener {
            findNavController().popBackStack()
        }
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressRegister.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
                    binding.tilDisplayName.error = state.displayNameError
                    binding.tilEmail.error = state.emailError
                    binding.tilPassword.error = state.passwordError
                    binding.tilConfirmPassword.error = state.confirmPasswordError
                    binding.tvRegisterStatus.text = state.statusMessage.orEmpty()
                    binding.ivRegisterAvatar.renderAvatar(state.selectedAvatarId)

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
