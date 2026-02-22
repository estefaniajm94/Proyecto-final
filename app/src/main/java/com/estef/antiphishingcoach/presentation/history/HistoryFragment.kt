package com.estef.antiphishingcoach.presentation.history

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.privacy.LocalAuthManager
import com.estef.antiphishingcoach.databinding.FragmentHistoryBinding
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import kotlinx.coroutines.launch

class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    R.layout.fragment_history,
    FragmentHistoryBinding::bind
) {
    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(
            observeHistoryUseCase = appContainer().observeHistoryUseCase,
            observeExtremePrivacyUseCase = appContainer().observeExtremePrivacyUseCase
        )
    }
    private val localAuthManager = LocalAuthManager()
    private lateinit var historyAdapter: HistoryAdapter
    private var contentStarted = false
    private var accessValidated = false
    private var authInProgress = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        setLockedStateVisible(false)
    }

    override fun onResume() {
        super.onResume()
        enforceProtectedAccess()
    }

    private fun setupList() = with(binding) {
        historyAdapter = HistoryAdapter { incidentId ->
            val action = HistoryFragmentDirections.actionHistoryToHistoryDetail(incidentId)
            findNavController().navigate(action)
        }
        rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupControls() = with(binding) {
        etHistorySearch.doAfterTextChanged { editable ->
            viewModel.onQueryChanged(editable?.toString().orEmpty())
        }

        chipGroupTrafficFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val checked = checkedIds.firstOrNull()
            val filter = when (checked) {
                R.id.chipFilterGreen -> HistoryTrafficLightFilter.GREEN
                R.id.chipFilterYellow -> HistoryTrafficLightFilter.YELLOW
                R.id.chipFilterRed -> HistoryTrafficLightFilter.RED
                else -> HistoryTrafficLightFilter.ALL
            }
            viewModel.onTrafficLightFilterChanged(filter)
        }

        val orderOptions = listOf("Mas reciente", "Mayor riesgo")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, orderOptions)
        actvHistorySort.setAdapter(sortAdapter)
        actvHistorySort.setText(orderOptions.first(), false)
        actvHistorySort.setOnItemClickListener { _, _, position, _ ->
            val mode = if (position == 1) {
                HistorySortMode.HIGHEST_RISK
            } else {
                HistorySortMode.MOST_RECENT
            }
            viewModel.onSortModeChanged(mode)
        }

        btnGoAnalyzeFromHistory.setOnClickListener {
            findNavController().navigate(R.id.analyzeFragment)
        }
    }

    private fun enforceProtectedAccess() {
        viewLifecycleOwner.lifecycleScope.launch {
            val localLockEnabled = appContainer().isLocalLockEnabledUseCase()
            if (!localLockEnabled) {
                accessValidated = true
                startContentIfNeeded()
                return@launch
            }
            if (accessValidated) {
                startContentIfNeeded()
                return@launch
            }
            if (!localAuthManager.canAuthenticate(requireActivity())) {
                findNavController().popBackStack()
                return@launch
            }
            setLockedStateVisible(true)
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
                    startContentIfNeeded()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    authInProgress = false
                    if (!isAdded) return
                    findNavController().popBackStack()
                }
            }
        )
        prompt.authenticate(
            localAuthManager.buildPromptInfo(
                title = "Desbloquear historial",
                subtitle = "Verifica tu identidad para continuar"
            )
        )
    }

    private fun startContentIfNeeded() {
        setLockedStateVisible(false)
        if (contentStarted) return
        contentStarted = true
        setupList()
        setupControls()
        observeUiState()
    }

    private fun setLockedStateVisible(isLocked: Boolean) = with(binding) {
        tvHistoryLocked.isVisible = isLocked
        contentHistory.isVisible = !isLocked
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressHistory.isVisible = state.isLoading
                    binding.tvHistoryEmpty.isVisible = !state.isLoading && state.items.isEmpty()
                    binding.tvHistoryEmpty.text = state.emptyMessage
                    binding.btnGoAnalyzeFromHistory.isVisible = !state.isLoading && state.items.isEmpty()
                    historyAdapter.submitList(state.items)
                }
            }
        }
    }
}
