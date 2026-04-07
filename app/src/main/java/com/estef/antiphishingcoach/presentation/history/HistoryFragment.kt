package com.estef.antiphishingcoach.presentation.history

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.estef.antiphishingcoach.R
import com.estef.antiphishingcoach.core.privacy.LocalAuthManager
import com.estef.antiphishingcoach.databinding.FragmentHistoryBinding
import com.estef.antiphishingcoach.presentation.common.AndroidStringResolver
import com.estef.antiphishingcoach.presentation.common.BaseFragment
import com.estef.antiphishingcoach.presentation.common.appContainer
import com.estef.antiphishingcoach.presentation.common.collectOnStarted
import com.estef.antiphishingcoach.presentation.common.viewModelFactory
import kotlinx.coroutines.launch

/**
 * Pantalla de historial privado. Mantiene la lista ligada al ciclo de vida de la vista
 * y usa la autenticación local solo como compuerta de acceso visual.
 */
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    R.layout.fragment_history,
    FragmentHistoryBinding::bind
) {
    private val viewModel: HistoryViewModel by viewModels {
        val c = appContainer()
        viewModelFactory {
            HistoryViewModel(
                observeHistoryUseCase = c.observeHistoryUseCase,
                observeExtremePrivacyUseCase = c.observeExtremePrivacyUseCase,
                stringResolver = AndroidStringResolver(requireContext().applicationContext)
            )
        }
    }
    private val localAuthManager = LocalAuthManager()
    private lateinit var historyAdapter: HistoryAdapter
    private var accessValidated = false
    private var authInProgress = false

    override fun onBoundView(savedInstanceState: Bundle?) {
        setupBackNavigation(binding.btnBack)
        setupList()
        setupControls()
        // El collector se registra aquí para que repeatOnLifecycle quede asociado
        // al ciclo de vida correcto de la vista, no al regreso desde onResume.
        collectOnStarted(viewModel.uiState) { state ->
            binding.progressHistory.isVisible = state.isLoading
            binding.tvHistoryEmpty.isVisible = !state.isLoading && state.items.isEmpty()
            binding.tvHistoryEmpty.text = state.emptyMessage
            binding.btnGoAnalyzeFromHistory.isVisible = !state.isLoading && state.items.isEmpty()
            historyAdapter.submitList(state.items)
        }
        setLockedStateVisible(false)
    }

    override fun onResume() {
        super.onResume()
        // La biometría se revalida al volver a primer plano sin duplicar collectors.
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
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipFilterGreen -> HistoryTrafficLightFilter.GREEN
                R.id.chipFilterYellow -> HistoryTrafficLightFilter.YELLOW
                R.id.chipFilterRed -> HistoryTrafficLightFilter.RED
                else -> HistoryTrafficLightFilter.ALL
            }
            viewModel.onTrafficLightFilterChanged(filter)
        }

        val orderOptions = resources.getStringArray(R.array.history_sort_options).toList()
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, orderOptions)
        actvHistorySort.setAdapter(sortAdapter)
        actvHistorySort.setText(orderOptions.first(), false)
        actvHistorySort.setOnItemClickListener { _, _, position, _ ->
            val mode = if (position == 1) HistorySortMode.HIGHEST_RISK else HistorySortMode.MOST_RECENT
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
                setLockedStateVisible(false)
                return@launch
            }
            if (accessValidated) {
                setLockedStateVisible(false)
                return@launch
            }
            if (!localAuthManager.canAuthenticate(requireActivity())) {
                findNavController().popBackStack()
                return@launch
            }
            setLockedStateVisible(true)
            if (!authInProgress) requestAuthentication()
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
                    setLockedStateVisible(false)
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
                title = getString(R.string.history_unlock_title),
                subtitle = getString(R.string.history_unlock_subtitle)
            )
        )
    }

    private fun setLockedStateVisible(isLocked: Boolean) = with(binding) {
        tvHistoryLocked.isVisible = isLocked
        contentHistory.isVisible = !isLocked
    }
}
