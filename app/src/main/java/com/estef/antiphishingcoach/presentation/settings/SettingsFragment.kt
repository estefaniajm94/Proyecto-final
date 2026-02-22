package com.estef.antiphishingcoach.presentation.settings

import android.os.Bundle
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.privacy.LocalAuthManager
import com.estef.antiphishingcoach.databinding.FragmentSettingsBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import kotlinx.coroutines.launch

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    R.layout.fragment_settings,
    FragmentSettingsBinding::bind
) {
    private val viewModel: SettingsViewModel by viewModels {
        val container = appContainer()
        SettingsViewModelFactory(
            observeExtremePrivacyUseCase = container.observeExtremePrivacyUseCase,
            observeLocalLockUseCase = container.observeLocalLockUseCase,
            toggleExtremePrivacyUseCase = container.toggleExtremePrivacyUseCase,
            toggleLocalLockUseCase = container.toggleLocalLockUseCase,
            clearLocalDataUseCase = container.clearLocalDataUseCase
        )
    }
    private val localAuthManager = LocalAuthManager()
    private var accessValidated = false
    private var authInProgress = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        setProtectedControlsEnabled(false)
        setupActions()
        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        enforceProtectedAccess()
    }

    private fun setupActions() = with(binding) {
        switchExtremePrivacy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != viewModel.uiState.value.extremePrivacyEnabled) {
                viewModel.onExtremePrivacyChanged(isChecked)
            }
        }
        switchLocalLock.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == viewModel.uiState.value.localLockEnabled) {
                return@setOnCheckedChangeListener
            }
            if (isChecked && !localAuthManager.canAuthenticate(requireActivity())) {
                switchLocalLock.isChecked = false
                viewModel.onLocalLockNotAvailable()
                return@setOnCheckedChangeListener
            }
            viewModel.onLocalLockChanged(isChecked)
        }
        btnClearData.setOnClickListener {
            viewModel.clearLocalData()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.switchExtremePrivacy.isChecked = state.extremePrivacyEnabled
                    binding.switchLocalLock.isChecked = state.localLockEnabled
                    binding.tvSettingsStatus.text = state.statusMessage.orEmpty()
                }
            }
        }
    }

    private fun enforceProtectedAccess() {
        viewLifecycleOwner.lifecycleScope.launch {
            val localLockEnabled = appContainer().isLocalLockEnabledUseCase()
            if (!localLockEnabled) {
                accessValidated = true
                setProtectedControlsEnabled(true)
                return@launch
            }
            if (accessValidated) {
                setProtectedControlsEnabled(true)
                return@launch
            }
            if (!localAuthManager.canAuthenticate(requireActivity())) {
                viewModel.onAccessBlockedByAuthError()
                findNavController().popBackStack()
                return@launch
            }
            setProtectedControlsEnabled(false)
            if (!authInProgress) {
                requestAuthentication()
            }
        }
    }

    private fun requestAuthentication() {
        authInProgress = true
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(requireContext()),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authInProgress = false
                    accessValidated = true
                    if (!isAdded) return
                    setProtectedControlsEnabled(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    authInProgress = false
                    if (!isAdded) return
                    viewModel.onAccessBlockedByAuthError()
                    findNavController().popBackStack()
                }
            }
        )
        prompt.authenticate(
            localAuthManager.buildPromptInfo(
                title = "Desbloquear ajustes",
                subtitle = "Verifica tu identidad para continuar"
            )
        )
    }

    private fun setProtectedControlsEnabled(enabled: Boolean) = with(binding) {
        switchExtremePrivacy.isEnabled = enabled
        switchLocalLock.isEnabled = enabled
        btnClearData.isEnabled = enabled
    }
}
